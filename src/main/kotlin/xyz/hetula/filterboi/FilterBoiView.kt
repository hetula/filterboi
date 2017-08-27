/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.hetula.filterboi

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.util.*

/**
 * @author Tuomo Heino
 * @version 27.8.2017.
 */
class FilterBoiView : Initializable, FilterBoiContract.View {
    @FXML
    private var txtFilter: TextField? = null

    @FXML
    private var chkNotMatching: CheckBox? = null

    @FXML
    private var chkRegex: CheckBox? = null

    @FXML
    private var logArea: TextArea? = null

    @FXML
    private var logScrollBar: ScrollBar? = null

    @FXML
    private var lblLines: Label? = null

    @FXML
    private var lblSearchTime: Label? = null

    private var lineHeight: Double = 0.0
    private var selChooser: FileChooser? = null

    var presenter: FilterBoiContract.Presenter? = null
        internal set

    var window: FilterBoiContract.Window? = null
        internal set

    @FXML
    private fun onNotMatchingClick(event: ActionEvent) {
        filter(txtFilter?.text!!)
    }

    @FXML
    private fun onRegexClick(event: ActionEvent) {
        filter(txtFilter?.text!!)
    }

    @FXML
    private fun onImportClick(event: ActionEvent) {
        val f = selChooser?.showOpenDialog(window?.getPrimaryStage()) ?: return
        presenter?.importLog(f.toPath())
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selChooser = FileChooser()
        selChooser?.title = "Select log file"
        selChooser?.initialDirectory = File(System.getProperty("user.home"))

        logScrollBar?.valueProperty()?.addListener { _, _, line ->
            presenter?.setContent(line.toInt())
        }

        txtFilter?.textProperty()?.addListener { _, _, text -> filter(text) }

        // Run after initial setup to gather some data
        Platform.runLater {
            val logScroller = logArea?.lookup(".scroll-bar:vertical")
            logScroller?.isDisable = true
            val font = logArea?.font
            lineHeight = Utils.computeTextHeight(font!!, "|", 123.0)
        }
    }

    override fun showScrollBar(show: Boolean) {
        logScrollBar?.isVisible = show
    }

    override fun setScrollBar(max: Int) {
        logScrollBar?.min = 0.0
        logScrollBar?.unitIncrement = 1.0
        logScrollBar?.blockIncrement = (max / 25).toDouble()
        logScrollBar?.max = max.toDouble()
    }

    override fun resetScrollBar() {
        logScrollBar?.value = 0.0
    }

    override fun clearText() {
        logArea?.clear()
    }

    override fun appendText(line: String) {
        if (logArea?.text!!.isNotEmpty()) {
            logArea?.appendText(System.lineSeparator())
        }
        logArea?.appendText(line)
        logArea?.positionCaret(0)
    }

    override fun setLines(lines: Int) {
        lblLines?.text = "Lines: $lines"
    }

    override fun setSearchTook(ms: Long) {
        lblSearchTime?.text = "Searh took: $ms ms"
    }

    override fun getVisibleRowCount(): Int {
        return (logArea!!.height / lineHeight).toInt()
    }

    private fun filter(filterText: String) {
        presenter?.doSearch(Filter(filterText, chkNotMatching!!.isSelected, chkRegex!!.isSelected))
    }
}