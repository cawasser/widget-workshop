# Widget Workshop

A new repo to experiment with a simpler, more consistent, and (hopefully) more general
approach to creating UI widgets for browser-based "client" software.


## Development

Clone *both* repos:

    git clone https://github.com/cawasser/widget-workshop.git

    git clone https://github.com/cawasser/reagent-table.git


You first need to compile and "install" the react-table, as we've made changed to make
it compatible with the latest Reagent


Run:
    lein uberjar

    lein install

This compiles the library and installs it into your *local* Maven repository. Don't  
worry, shadow-cljs can find it automatically by the new veriosn number.

Now Run:

    npm install
    shadow-cljs watch app

Open your browser to:

    [http://localhost:3333](http://localhost:3333)

## Tooling


### Cursive

Import the project into Cursive using "File | Project | Project form existing sources...".  
In the popup, navigate into your new folder, and select the "pom.xlm" file. Click OK.


### nREPL

An nREPL is automatically started on port 7777.

## Version History and Goals

Currently this code replicated the client-only portion of Sample Task #2, providing a simple
[Reagent](https://github.com/reagent-project/reagent)-based "equation" UI to play with. The
server-side is not included as this ia a shadow-cljs only project. The built-in
[http-server](https://shadow-cljs.github.io/docs/UsersGuide.html#http) is used only to load
the SPA into the browser.

Next? Move to [Re-frame](https://github.com/Day8/re-frame) (including [re-frame-10x](https://github.com/Day8/re-frame-10x))
and then start looking at the widget creation sequencing.

## The Story So Far...

Drag & Drop code mimics:

https://github.com/atlassian/react-beautiful-dnd/issues/427#issuecomment-420563943

"the story so far..."

seems that Draggables must be inside a Droppable, not just inside a DragDropContext. this is
just what the JS tutorial, through part 4, does. Might be some other configuration that removes
this requirements (since we don't need to support dropping back on the data-sources list anyway)


## Danger Will Robinson!

If you need to update the react-table library, be sure to both:

1. update the version number in project.clj
2. run `lein uberjar`
3. run `lein install`
4. restart `shadow-cljs watch :app` so it picks recognized the updated CLJS dependency(ies)
