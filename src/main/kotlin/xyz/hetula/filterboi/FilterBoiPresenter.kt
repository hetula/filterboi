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
import javafx.scene.input.ScrollEvent
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * @author Tuomo Heino
 * @version 27.8.2017.
 */
class FilterBoiPresenter(private val view: FilterBoiContract.View,
                         private val window: FilterBoiContract.Window) : FilterBoiContract.Presenter {
    private val title = "Filter Boi"
    private val filterBoi = FilterBoi()

    init {
        window.setTitle(title)
    }

    override fun detach() {
        filterBoi.release()
    }

    override fun setContent(fromIndex: Int) {
        view.clearText()
        val rows = view.getVisibleRowCount()
        filterBoi.queryText(fromIndex, rows) {
            view.appendText(it)
        }
        view.showScrollBar(getCurrentLines() > rows)
    }

    override fun importLog(file: Path, encoding: Charset) {
        filterBoi.loadFile(file, encoding) {
            window.setTitle("${file.toAbsolutePath()} - $title")
            setViewInfo()
            setContent(0)
        }
    }

    override fun saveLog(toPath: Path) {
        filterBoi.saveLog(toPath)
    }

    override fun getCurrentLines(): Int {
        return filterBoi.getCurrentLines()
    }

    override fun doSearch(filter: Filter) {
        filterBoi.filter(filter) {
            setViewInfo()
            setContent(0)
        }
    }

    override fun onResize() {
        Platform.runLater {
            view.refreshView()
        }
    }

    override fun onScroll(scrollEvent: ScrollEvent) {
        when (scrollEvent.textDeltaYUnits) {
            ScrollEvent.VerticalTextScrollUnits.NONE -> view.scrollPixels(scrollEvent.deltaY)
            ScrollEvent.VerticalTextScrollUnits.LINES -> view.scrollLines(scrollEvent.textDeltaY)
            ScrollEvent.VerticalTextScrollUnits.PAGES -> view.scrollPages(scrollEvent.textDeltaY)
            else -> System.out.println("Null event? $scrollEvent")
        }
    }

    private fun setViewInfo() {
        val lines = filterBoi.getCurrentLines()
        view.setLines(lines)
        view.setSearchTook(filterBoi.lastSearchDur)
        view.resetScrollBar()
        view.setScrollBar(lines)
    }
}