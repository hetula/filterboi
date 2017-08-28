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
import java.nio.file.Path

/**
 * @author Tuomo Heino
 * @version 27.8.2017.
 */
class FilterBoiPresenter(private val view: FilterBoiContract.View,
                         private val window: FilterBoiContract.Window) : FilterBoiContract.Presenter {
    private val title = "Filter Boi"

    init {
        window.setTitle(title)
    }

    override fun setContent(fromIndex: Int) {
        view.clearText()
        val rows = view.getVisibleRowCount()
        FilterBoi.queryText(fromIndex, rows) {
            view.appendText(it)
        }
        view.showScrollBar(getCurrentLines() > rows)
    }

    override fun importLog(file: Path) {
        FilterBoi.loadFile(file) {
            window.setTitle("${file.toAbsolutePath()} - $title")
            setViewInfo()
            setContent(0)
        }
    }

    override fun getCurrentLines(): Int {
        return FilterBoi.getCurrentLines()
    }

    override fun doSearch(filter: Filter) {
        FilterBoi.filter(filter) {
            setViewInfo()
            setContent(0)
        }
    }

    override fun onResize() {
        Platform.runLater {
            view.refreshView()
        }
    }

    private fun setViewInfo() {
        val lines = FilterBoi.getCurrentLines()
        view.setLines(lines)
        view.setSearchTook(FilterBoi.lastSearchDur)
        view.resetScrollBar()
        view.setScrollBar(lines)
    }
}