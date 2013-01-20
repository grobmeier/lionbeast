# Lionbeast

This is a small web server demonstrating some basic features on Java NIO.

Please cd into ./server to carry out most operations. The parent pom is not utilized that much,
except for dependency management.

Build the distribution with:

$> mvn package

This will also run the assembly:single goal, which creates a ZIP package with a ready-to-go server.

## Introduction

The server first creates a single thread which listens to a specific interface with a specific port (the Dispatcher).
Configuration of these values is done in lionbeast-server.xml.

Incoming requests are then dispatched to various other threads, called Workers. Workers get already
the read header information, but are responsible themselves to take care on HTTP methods or content.

For the moment, only GET without content is supported.

The Worker selects an appropriate Handler for a request, based on either complete path
(like: http://localhost:10000/helloworld) or on file extension (like: .html). This selection is done by
Matchers; they are configured in lionbeast-matchers.xml. When a matcher is found, the handler will be selected
based on Matcher information. Handlers are defined in lionbeast-handlers.xml.

For the configuration, Apache Commons Configuration has been used. Currently its implemented as Singleton; it is
usually a better Idea to use a dependency injection framework for that. At the moment the Configuration class
is coupled to tightly and has cause some annoyance when writing unit tests.

The Handlers are written in the NIO vein. A pipe has been used to read the content from disk (in case of
a FileHandler). Since a Pipe.sink would not accept more bytes once it is full, a new Thread just for reading
the content from disc was necessary. It needs to be considered if this is actually a benefit or not; usually
it is said one should keep the number of threads as low as possible. Also the FileHandler has become a bit
more complex than necessary. Instead of a NIO Pipe a classic InputStream could have been used.

The JRuby handler is just for demonstration and should not be taken to serious. The instantiation
of the ScriptingEngine does need a good amount of time and could be done somewhere else. Also setting
of the WriterWrapper does take its time; in addition the WriterWrapper class is just a skeleton and could
be improved a lot. Anyway it works and shows that JRuby - or maybe Groovy et al - is easily possible
to use with Lionbeast.

The HelloWorldHandler is another demonstration of in-memory-processing of a request. Instead of just
printing "Hello World", this handler could connect to a database/content-repository. As it does not serve
and purpose, it would make a good fit for the testing package. Its there for demonstration purposes.

The ServerStatusHandler is being called if something goes wrong, for example a file has not been found on hard disc.
In this case an exception is thrown and the handler is stopped. The Worker is in charge to resolve the situation.
It is a problem, when headers have already been sent. In this case nothing can be done anymore, especially
then Content-Length is critical. If the actual content does not match this number of bytes, the client is waiting
for ever for input. So it has been decided to just close the connection when some or all headers are sent.

If headers are not sent, the ServerStatusHandler can chime in; the connection can be kept open if Keep-Alive has
been requested. The Handler will take care an appropriate message is being sent to the Browser.

## Webdir configuration

In the distribution package and in src/test is a folder called "webdir". This is the folder from which the
content is served. The exact location of the webdir can be defined in lionbeast-server.xml.

## Testing

For testing some JUnit tests has been established. They are all pretty basic and could be more specific.
The most difficult test was the WorkerTest. Mockito helped already to mock some objects, but it was not possible
to mock final methods, even not with Powermock. Powermock is being said to work well with final-Methods which
I cannot confirm.

Testing and development has been done on OS X 10.6.8. Browsers were Firefox, Safari and Chrome, all recent versions.

As I do not have a Windows-Box for work, I could not test a start.bat. It should be similar to the provided start.sh
in src/cli.

## Development

These tools have been used: Bitbucket, Git, IntelliJ IDEA.

Dependencies are SLF4J, Log4j 2.0 (beta), Apache Commons Configuration.

## License

/*
 *   Copyright 2013 Christian Grobmeier
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

