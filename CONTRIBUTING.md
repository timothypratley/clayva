# Contributing

This project was derived from the [vscode-extension-template](https://github.com/PEZ/vscode-extension-template).

## Developing

1. <kbd>cmd/ctrl</kbd>+<kbd>shift</kbd>+<kbd>b</kbd>. This starts the default build task, which is configured (in [.vscode/tasks.json](.vscode/tasks.json) to start shadow-cljs watcher.
   * Let it compile the extension and run the tests.
1. <kbd>F5</kbd>. Starts the VS Code Development Extension host (because configured to do so in [.vscode/launch.json](.vscode/launch.json))
   * This is a VS Code window where your extension under development is installed.
1. In the extension development host <kbd>cmd/ctrl</kbd>+<kbd>shift</kbd>+<kbd>p</kbd>, find and run the command **Clayva: current form html**
   * This activates your extension and starts the ClojureScript REPL
1. Back in the development project run the command **Calva: Connect to a Running REPL Server in the Project**:
   1. Select the project root **my-extension**
   1. Select the project type **shadow-cljs**
   1. Select to connect to the build `:extension`
      * Now you can hack on the extension code and the extension in the development host window will be updated while it is running (interactive programming).

The important thing to note here is the steps where you activate your extension in the development host, starting the ClojureScript repl which Calva can connect to. Depending on the extension you build, it may be that it activates automatically, or by some specific file appearing or whatever. The point is that it needs to be started for Calva to connect to it, and it starting the development host is often not enough to start your extension. (You can actually start the Calva connect before you start the extension host. Calva will connect when the repl is started.)

Build information shows in the TERMINAL panel.
