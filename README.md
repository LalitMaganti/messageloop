MessageLoop
==========
A simple message (event) loop for the JVM.

```java

public class MessageHandler implements MessageLoop.Handler {
  // Executed on a background thread - the event loop.
  void handle(int type, Object obj) {
    if (type == READ_JSON) {
      JsonReader reader = (JsonReader) obj;
      reader.read();
      // Do other stuff.
    }
    // handle other type and obj.
  }
}

public class Main {
  public void run() {
    MessageHandler handler = new MessageHandler();
    MessageLoop loop = BlockingQueueMessageLoop.create(handler);

    // Carry out the reading on the event loop.
    loop.post(READ_JSON, new JsonReader()));
    // Do some more stuff with the loop.
  }
}

```

For too long, a lightweight message loop implementation for the JVM has been missing. Something which is equivalent to Android's
Handler and HanderThread classes.

Well no longer: MessageLoop is the library to fix this. It's main purpose is to define a generic and multi-purpose abstraction
of a message loop and an associated message handler. It further augments this by providing a reference implementation which uses
BlockingQueue as a backing data structure.

As well as the core implementation, there is a further android module which provides an implementation of these interfaces
by using the inspiring Handler and HandlerThread classes.

Download
----------
Maven release in progress.

License
---------
```
Copyright 2016 Lalit Maganti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
