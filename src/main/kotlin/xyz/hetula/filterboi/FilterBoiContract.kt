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

import javafx.stage.Stage
import java.nio.file.Path

/**
 * @author Tuomo Heino
 * @version 27.8.2017.
 */
interface FilterBoiContract {
    interface Window {
        fun setTitle(title: String)
        fun getPrimaryStage(): Stage
    }

    interface View {
        fun showScrollBar(show: Boolean)
        fun setScrollBar(max: Int)
        fun resetScrollBar()
        fun clearText()
        fun appendText(line: String)
        fun setLines(lines: Int)
        fun setSearchTook(ms: Long)
        fun getVisibleRowCount(): Int
        fun refreshView()
    }

    interface Presenter {
        fun doSearch(filter: Filter)
        fun setContent(fromIndex: Int)
        fun importLog(file: Path)
        fun getCurrentLines(): Int
        fun onResize()
    }
}