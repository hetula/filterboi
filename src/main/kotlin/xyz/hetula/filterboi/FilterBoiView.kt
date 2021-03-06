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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.ScrollEvent
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*

/**
 * @author Tuomo Heino
 * @version 27.8.2017.
 */
class FilterBoiView : Initializable, FilterBoiContract.View {
    @FXML
    private lateinit var txtFilter: TextField

    @FXML
    private lateinit var chkNotMatching: CheckBox

    @FXML
    private lateinit var chkRegex: CheckBox

    @FXML
    private lateinit var logArea: TextArea

    @FXML
    private lateinit var logScrollBar: ScrollBar

    @FXML
    private lateinit var lblLines: Label

    @FXML
    private lateinit var lblSearchTime: Label

    @FXML
    private lateinit var cbEncoding: ComboBox<String>

    private var lineHeight: Double = 0.0
    private lateinit var selChooser: FileChooser

    lateinit var presenter: FilterBoiContract.Presenter
        internal set

    lateinit var window: FilterBoiContract.Window
        internal set

    @FXML
    private fun onAllFilters() {

    }

    @FXML
    private fun onNotMatchingClick() {
        filter(txtFilter.text)
    }

    @FXML
    private fun onRegexClick() {
        filter(txtFilter.text)
    }

    @FXML
    private fun onImportClick() {
        val f = selChooser.showOpenDialog(window.getPrimaryStage()) ?: return
        val encoding = Charset.forName(cbEncoding.value) ?: return
        presenter.importLog(f.toPath(), encoding)
    }

    @FXML
    private fun onSaveLog() {
        val f = selChooser.showSaveDialog(window.getPrimaryStage()) ?: return
        presenter.saveLog(f.toPath())
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selChooser = FileChooser()
        selChooser.title = "Select log file"
        selChooser.initialDirectory = File(System.getProperty("user.home"))

        cbEncoding.items.addAll(Charset.availableCharsets().keys)
        cbEncoding.selectionModel.select(Charset.defaultCharset().name())

        logScrollBar.valueProperty().addListener { _, _, line ->
            presenter.setContent(line.toInt())
        }

        txtFilter.textProperty().addListener { _, _, text -> filter(text) }

        logArea.heightProperty().addListener { _, _, _ -> presenter.onResize() }
        logArea.widthProperty().addListener { _, _, _ -> presenter.onResize() }
        logArea.addEventFilter(ScrollEvent.SCROLL) {
            presenter.onScroll(it)
            it.consume()
        }

        // Run after initial setup to gather some data
        Platform.runLater {
            val logScroller = logArea.lookup(".scroll-bar:vertical")
            logScroller.isDisable = true
            lineHeight = Utils.computeTextHeight(logArea.font, "|", 123.0) + 1.75
        }
    }

    override fun showScrollBar(show: Boolean) {
        logScrollBar.isVisible = show
    }

    override fun setScrollBar(max: Int) {
        logScrollBar.min = 0.0
        logScrollBar.unitIncrement = 3.0
        logScrollBar.blockIncrement = (max / 25).toDouble()
        logScrollBar.max = Math.max(max - getVisibleRowCount() + 2, 0).toDouble()
    }

    override fun resetScrollBar() {
        logScrollBar.value = 0.0
    }

    override fun scrollPixels(pixels: Double) {
        if (pixels < 0) {
            logScrollBar.increment()
        } else {
            logScrollBar.decrement()
        }
    }

    override fun scrollLines(lines: Double) {
        println("Lines scroll: $lines")
        if (lines < 0) {
            logScrollBar.increment()
        } else {
            logScrollBar.decrement()
        }
    }

    override fun scrollPages(pages: Double) {
        println("Pages scroll: $pages")
        if (pages < 0) {
            logScrollBar.increment()
        } else {
            logScrollBar.decrement()
        }
    }

    override fun clearText() {
        logArea.clear()
    }

    override fun appendText(line: String) {
        if (logArea.text!!.isNotEmpty()) {
            logArea.appendText(System.lineSeparator())
        }
        logArea.appendText(line)
        logArea.positionCaret(0)
    }

    override fun setLines(lines: Int) {
        lblLines.text = "Lines: $lines"
    }

    override fun setSearchTook(ms: Long) {
        lblSearchTime.text = "Searh took: $ms ms"
    }

    override fun getVisibleRowCount(): Int {
        return (logArea.height / lineHeight).toInt()
    }

    override fun refreshView() {
        val line = logScrollBar.value
        presenter.setContent(line.toInt())
    }

    private fun filter(filterText: String) {
        presenter.doSearch(Filter(filterText, chkNotMatching.isSelected, chkRegex.isSelected))
    }
}