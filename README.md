# Cereal

***NOTE: this library is a work in progress and the API may change.***

A simple library for serial port communication with Clojure. Although serial communciation may be considered old tech, it's useful for a communicating with a plethora of devices including exciting new hardware such as the [Monome](http://monome.org) and the [Arduino](http://arduino.cc).  It's powerd by [PureJavaComm](https://github.com/nyholku/purejavacomm) for serial communication


## Installation

Add the following to your `deps.edn` dependency list:

```clojure
uk.axvr.cereal {:git/url "https://github.com/axvr/cereal.git"
                :sha     "0639daf4389a5934946ee1e19f2ab127c854815a"}}
```

## Usage

### Using the library

Require the `uk.axvr.cereal` namespace like so:

```clojure
(require '[uk.axvr.cereal :as serial])
```

### Finding your port identifier

In order to connect to your serial device you need to know the path of the file it presents itself on. `list-ports` will print these paths out:

```clojure
(serial/list-ports)
;; /dev/tty.usbmodemfa141
;; /dev/cu.usbmodemfa141
;; /dev/tty.Bluetooth-PDA-Sync
;; /dev/cu.Bluetooth-PDA-Sync
;; /dev/tty.Bluetooth-Modem
;; /dev/cu.Bluetooth-Modem
```

In this case, we have an Arduino connected to `/dev/tty.usbmodemfa141`.

### Connecting with a port identifier

When you know the path to the serial port, connecting is just as simple as:

```clojure
(serial/open "/dev/tty.usbmodemfa141")
```

However, you'll want to bind the result so you can use it later:

```clojure
(def port (serial/open "/dev/tty.usbmodemfa141"))
```

### Reading bytes

If you wish to get raw access to the `InputStream` this is possible with the function `listen!`. This allows you to specify a handler that will get called every time there is data available on the port and will pass your handler the `InputStream` to allow you to directly `.read` bytes from it.

When the handler is first registered, the bytes that have been buffered on the serial port are dropped by default. This can be changed by passing false to `listen!` as an optional last argument.

Only one listener may be registered at a time. If you want to fork the incoming datastream to a series of streams, you might want to consider using lamina. You can then register a handler which simply enqueues the incoming serial data to a lamina channel which you may then fork and map according to your whim.

Finally, you may remove your listener by calling `unlisten!` and passing it the port binding.

### Writing bytes

The simplest way to write bytes is by passing a byte array to `write`:

```clojure
(serial/write port my-byte-array)
```

This also works with any `Number`

```clojure
(serial/write port 20)
```

As well as any `Sequential`

```clojure
(serial/write port [0xf0 0x79 0xf7])
```

### Closing the port

Simply use the `close!` function:

```clojure
(serial/close! port)
```

## Contributors

* [peterschwarz/clj-serial](https://github.com/peterschwarz/clj-serial)
  * Peter Schwarz
* [samaaron/serial-port](https://github.com/samaaron/serial-port)
  * Sam Aaron
  * Jeff Rose

## Legal

Distributed under the Eclipse Public License v1.0 (the same as Clojure).
