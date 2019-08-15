from pi_ui.mainscreen import WaittingScreen
from PyQt5.QtWidgets import *
import sys




def main():

    app = QApplication(sys.argv)
    app.processEvents()
    ex = WaittingScreen()
    sys.exit(app.exec_())

if __name__ == '__main__':
    main()