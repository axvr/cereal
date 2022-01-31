# Cereal

**Unmaintained**: this library is no longer maintained because both Java and
the library this wrapped had limitations which prevented me from building what
I wanted it for.  If you would like to maintain it, please send me an email.

---

_**Note: this library is a work in progress and the API is likely to change.**_

A simple library for [serial port communication][serial] with Clojure.
Although serial communciation may be considered old tech, it's useful for
a communicating with a plethora of devices such as [Arduino][]s.


## Installation

Add the following to your `deps.edn` dependency list:

```clojure
uk.axvr/cereal {:git/url "https://github.com/axvr/cereal.git"
                :git/sha "8a0c4fea050fbb10099ffbeac1423b492ae4c678"}}
```


## Usage

Require the `uk.axvr.cereal` namespace like so:

```clojure
(require '[uk.axvr.cereal :as serial])
```


### Finding the port identifier

To connect to a serial device you need to know the identifier of the port the
device is connected to.  The `list-ports` function may help you find it.

```clojure
(serial/list-ports)
;; /dev/tty.usbmodemfa141
;; /dev/cu.usbmodemfa141
;; /dev/tty.Bluetooth-PDA-Sync
;; /dev/cu.Bluetooth-PDA-Sync
;; /dev/tty.Bluetooth-Modem
;; /dev/cu.Bluetooth-Modem
```

In this case, we have an Arduino connected at `/dev/tty.usbmodemfa141`.


### Connecting to a port

When you know the identifier of the serial port, use the `open` function to
open a connection to it.

```clojure
(def port (serial/open "/dev/tty.usbmodemfa141"))
```


### Reading bytes

<!-- TODO: rewrite this section. -->

If you wish to get raw access to the `InputStream` this is possible with the
function `listen!`. This allows you to specify a handler that will get called
every time there is data available on the port and will pass your handler the
`InputStream` to allow you to directly `.read` bytes from it.

When the handler is first registered, the bytes that have been buffered on the
serial port are dropped by default. This can be changed by passing false to
`listen!` as an optional last argument.

Only one listener may be registered at a time. If you want to fork the incoming
datastream to a series of streams, you might want to consider using lamina. You
can then register a handler which simply enqueues the incoming serial data to
a lamina channel which you may then fork and map according to your whim.

Finally, you may remove your listener by calling `unlisten!` and passing it the
port binding.


### Writing bytes

The simplest way to write bytes is by passing a byte array to `write!`:

```clojure
(serial/write! port my-byte-array)
```

This also works with any `Number`

```clojure
(serial/write! port 20)
```

As well as any `Sequential`

```clojure
(serial/write! port [0xf0 0x79 0xf7])
```


### Closing the port

Be sure to close your connection to the serial port once you are done using it.

```clojure
(serial/close! port)
```


## Legal

- Copyright © 2021, Alex Vear.
- Copyright © 2014–2018, Peter Schwarz.
- Copyright © 2011–2012, Sam Aaron.

Distributed under the Eclipse Public License v1.0 (the same as Clojure).

Cereal is a fork of Peter Schwarz's [clj-serial][peterschwarz/clj-serial] which
in turn was a fork of Sam Aaron's [serial-port][samaaron/serial-port].


[peterschwarz/clj-serial]: https://github.com/peterschwarz/clj-serial
[samaaron/serial-port]: https://github.com/samaaron/serial-port
[serial]: https://en.wikipedia.org/wiki/Serial_communication
[Arduino]: https://www.arduino.cc
