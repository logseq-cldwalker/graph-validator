# nbb-action-example

This is an example of how to use [nbb](https://github.com/borkdude/nbb), an
ad-hoc CLJS scripting tool for Node.js, to implement a Github action.

This action prints "Hello World" or "Hello" + the name of a person to greet to the log.

It was created using
[this](https://docs.github.com/en/actions/creating-actions/creating-a-javascript-action)
tutorial but instead of JavaScript it uses a small JS wrapper `index.mjs` + the
actual CLJS script `action.cljs`.

All JS dependencies, including nbb, are compiled into a single file,
`dist/index.mjs`, using [ncc](https://github.com/vercel/ncc/). This is
recommended in the Github docs as an alternative to commiting your
`node_modules` into git. Because `ncc` needs to statically know which
dependencies are required, this is done in the `index.mjs` wrapper and not
inside the `action.cljs` script. The dependencies are then passed as parameters
into a function defined in `action.cljs`.

Because Github actions uses an old version of Node.js (12, see
[issue](https://github.com/actions/runner/issues/772)), the action currently
uses a workaround in `run.cjs`: it forks to the system-wide installed version of
Node.js which is `14.17.6` at this time of writing.

## Inputs

## `who-to-greet`

**Required** The name of the person to greet. Default `"World"`.

## Outputs

## `time`

The time we greeted you.

## Example usage

``` yaml
on: [push]

jobs:
  hello_world_job:
    runs-on: ubuntu-latest
    name: A job to say hello
    steps:
      - name: Hello world action step
        id: hello
        uses: borkdude/nbb-action-example@v0.0.2
        with:
          who-to-greet: 'Mona the Octocat'
      # Use the output from the `hello` step
      - name: Get the output time
        run: echo "The time was ${{ steps.hello.outputs.time }}"
```

## Develop

See `bb tasks` for relevant tasks for development.
