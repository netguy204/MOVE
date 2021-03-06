MOVE
====

This is an attempt to put-to-code some of the concepts described in:

* Time to MOVE On: http://cirw.in/blog/time-to-move-on

* Architecture, The Lost Years: http://www.confreaks.com/videos/759-rubymidwest2011-keynote-architecture-the-lost-years

* http://collectiveidea.com/blog/archives/2012/06/28/wheres-your-business-logic/

The big idea is that it decomposes the application into Models, Views,
and Operations instead of Models, Views, and Controllers. What's the
difference? In my understanding (and experience thus far) this
seperation (like MVC) keep views and models from knowing anything
about each other. Unlike MVC, it does away with the ambiguous
"Controller" and encourages "Operations" instead. Operations
are essentially your high level use-cases in code form.

It's still a cloudy thing in my brain. That's why I'm writing code. I
can't say that I've reached any sort of enlightenment so far but there
are a view incidental things here that I like:

DataModelOperations is a protocol describing all of the things I do to
my data model. WebApplicationState implements this protocol and
provides an in-browser datastore. The server side could have an
implementation of this protocol that stored data in MySQL
instead. That's pretty cool.

ViewOperations is a protocol describing all of the things I do to my
global view. From the perspective of my operations, I've found it to
be quite useful to think of my view as a monolithic thing--basically
the whole browser window. My ViewOperations protocol (I'm pretending
to write a todo list) exposes high-level concepts like
"set-list-title" and emits high level events like
"create-clicked". Since the view is fat and global, events can be
global too. That's convenient. Hasn't bitten me yet though I'm wary.

For testing, I can mock out my monolithic view and fire those high
level events myself. make-noop-view does just that.

License
-------

Copyright (C) 2012 Brian Taylor

Distributed under the Eclipse Public License, the same as Clojure.
