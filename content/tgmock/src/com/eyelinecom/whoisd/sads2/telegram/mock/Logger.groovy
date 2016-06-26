package com.eyelinecom.whoisd.sads2.telegram.mock

trait Logger {

  boolean debug = false

  private File outFile

  void printInit(String path) {
    assert !outFile

    outFile = new File(path)

    _printHeader "[New run at ${new Date().format('dd.MM.yyyy HH:mm')}]"
    _printLine()
  }

  void _print(_) {
    _prints "$_\n"
  }

  synchronized void _prints(_) {
    System.out.print _
    outFile.append _
  }

  void _debug(_) {
    if (debug) {
      _print " $_"
    }
  }

  void _printHeader(_) {
    _print('\n' + _.center(80, "="))
  }

  void _printLine() {
    _print ''.center(80, "=")
  }

}
