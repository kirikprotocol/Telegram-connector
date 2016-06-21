package com.eyelinecom.whoisd.sads2.telegram.mock

trait Logger {

  private File outFile

  void printInit(String path) {
    assert !outFile

    outFile = new File(path)

    _printHeader "[New run at ${new Date().format('dd.MM.yyyy HH:mm')}]"
    _printLine()
  }

  void printInit() {
    printInit '/tmp/tg-mock.log'
  }

  synchronized void _print(_) {
    System.out.println _
    outFile.append(_ + "\n")
  }

  void _printHeader(_) {
    _print('\n' + _.center(80, "="))
  }

  void _printLine() {
    _print ''.center(80, "=")
  }

  void _fatal(_) {
    _print _

    throw new Exception(String.valueOf(_))
  }

}
