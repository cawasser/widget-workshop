# Widget Workshop

A new repo to experiment with a simpler, more consistent, and (hopefully) more general
approach to creating UI widgets for browser-based "client" software.


## Development

Clone the repo:

    git clone https://github.com/cawasser/widget-workshop.git

Run:

    npm install
    shadow-cljs watch app


## Version History and Goals

Currently this code replicated the client-only portion of Sample Task #2, providing a simple
[Reagent](https://github.com/reagent-project/reagent)-based "equation" UI to play with. The
server-side is not included as this ia a shadow-cljs only project. The built-in
[http-server](https://shadow-cljs.github.io/docs/UsersGuide.html#http) is used only to load
the SPA into the browser.

Next? Move to [Re-frame](https://github.com/Day8/re-frame) (including [re-frame-10x](https://github.com/Day8/re-frame-10x))
and then start looking at the widget creation sequencing.

