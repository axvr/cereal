(ns uk.axvr.cereal
  (:import [purejavacomm CommPortIdentifier
                         SerialPort
                         SerialPortEventListener
                         SerialPortEvent]
           [java.io Closeable OutputStream InputStream]
           [java.util UUID]))


(defprotocol Bytable
  (to-bytes [this] "Converts the type to bytes"))

(extend-protocol Bytable
  (class (byte-array 0))
  (to-bytes [this] this)

  Number
  (to-bytes [this]
    (byte-array 1 (.byteValue this)))

  clojure.lang.Sequential
  (to-bytes [this]
    (byte-array (count this) (map to-bytes this))))


(defrecord Port [port-id raw-port out-stream in-stream]
  Closeable
  (close [_]
    (doto ^SerialPort raw-port
      (.removeEventListener)
      (.close))))


(defn send!
  "Sends the given data to the port and returns it. All number literals are
  treated as bytes.

  By extending the protocol Bytable, any arbitry values can be sent to the
  output stream.  For example:

      (extend-protocol Bytable
        String
        (to-bytes [this] (.getBytes this \"ASCII\")))"
  [port & data]
  (let [^OutputStream out (:out-stream port)]
    (doseq [x data]
      (.write out ^bytes (to-bytes x))
      (.flush out))
    port))


(defn- listen!
  "Register a function to be called for every byte received on the specified port."
  [^SerialPort port ^InputStream in-stream handler]
  (let [listener (reify SerialPortEventListener
                   (serialEvent [_ event]
                     (when (= SerialPortEvent/DATA_AVAILABLE
                              (.getEventType event))
                       (handler in-stream))))]
    (doto port
      (.addEventListener listener)
      (.notifyOnDataAvailable true))))


(defn- ->parity [parity]
  (case parity
    :none  SerialPort/PARITY_NONE
    :odd   SerialPort/PARITY_ODD
    :even  SerialPort/PARITY_EVEN
    :mark  SerialPort/PARITY_MARK
    :space SerialPort/PARITY_SPACE))


(defn- ->data-bits [data-bits]
  (case data-bits
    5 SerialPort/DATABITS_5
    6 SerialPort/DATABITS_6
    7 SerialPort/DATABITS_7
    8 SerialPort/DATABITS_8))


(defn- ->stop-bits [stop-bits]
  (case stop-bits
    1   SerialPort/STOPBITS_1
    1.5 SerialPort/STOPBITS_1_5
    2   SerialPort/STOPBITS_2))


(defn- ->flow-control [flow-control]
  (case flow-control
    :none         SerialPort/FLOWCONTROL_NONE
    :rts-cts-in   SerialPort/FLOWCONTROL_RTSCTS_IN
    :rts-cts-out  SerialPort/FLOWCONTROL_RTSCTS_OUT
    :xon-xoff-in  SerialPort/FLOWCONTROL_XONXOFF_IN
    :xon-xoff-out SerialPort/FLOWCONTROL_XONXOFF_OUT))


(defn open
  "Returns an opened serial port.  Can specify:

    :baud-rate  (default 115200)

    :stop-bits
      1      (default)
      1.5
      2

    :data-bits
      5
      6
      7
      8      (default)

    :parity
      :none  (default)
      :odd
      :even
      :mark
      :space

    :flow-control
      :none  (default)
      :rts-cts-in
      :rts-cts-out
      :xon-xoff-in
      :xon-xoff-out

    :timeout  - in milliseconds (default 2000)

    :owner    - current owner of the port (defaults to random UUID)

    :listen   - a callback function invoked upon data received.

  These options can be set like so:

      (open \"/dev/ttyUSB0\")

      (open \"/dev/ttyUSB0\" :baud-rate 9600, :parity :none, :data-bits 8)"
  [port-id & {:keys [baud-rate
                     data-bits
                     stop-bits
                     parity
                     flow-control
                     timeout
                     owner
                     listen]
              :or {baud-rate    115200
                   data-bits    8
                   stop-bits    1
                   parity       :none
                   flow-control :none
                   timeout      2000}}]
  (let [owner    (or owner (str (UUID/randomUUID)))
        port-id  (CommPortIdentifier/getPortIdentifier port-id)
        raw-port ^SerialPort   (.open port-id owner timeout)
        out      ^OutputStream (.getOutputStream raw-port)
        in       ^InputStream  (.getInputStream  raw-port)]
    (when listen
      (listen! raw-port in listen))
    (doto raw-port
      (.setSerialPortParams baud-rate
                            (->data-bits data-bits)
                            (->stop-bits stop-bits)
                            (->parity parity))
      (.setFlowControlMode (->flow-control flow-control)))
    (Port. port-id raw-port out in)))


;;; ------------------------------------------------------------
;;; Utilities


(defn- port-identifiers
  "Returns a seq representing all port identifiers visible to the system."
  []
  (enumeration-seq
    (CommPortIdentifier/getPortIdentifiers)))


(defn get-ports
  "Returns a hash-set of avilable port IDs."
  []
  (->> (port-identifiers)
       (map #(.getName ^CommPortIdentifier %))
       set))


(defn list-ports
  "Print the IDs of available ports."
  []
  (doseq [port (get-ports)]
    (println port)))
