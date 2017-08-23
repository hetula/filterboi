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
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Tuomo Heino
 * @version 23.8.2017.
 */
object FilterBoi {
    private val taskQueue = Executors.newSingleThreadExecutor()
    private val originalContent: MutableList<String> = ArrayList()
    private val curFilteredContent: MutableList<String> = ArrayList()

    private var filter: Filter = Filter("")
    private var lastSearchDur = ""

    fun setText(view: FilterBoiView) {
        view.clearText()
        curFilteredContent.forEach { view.appendText(it + System.lineSeparator()) }
        view.setToolbarText(lastSearchDur)
    }

    fun loadFile(file: Path, view: FilterBoiView) {
        taskQueue.submit {
            originalContent.clear()
            curFilteredContent.clear()
            originalContent.addAll(Files.readAllLines(file))
            curFilteredContent.addAll(originalContent)
            doFiltering(filter, curFilteredContent)
            Platform.runLater {
                setText(view)
            }
        }
    }

    fun initialize() {
        println("FilterBoi Ready to Rumble!")
    }

    fun release() {
        taskQueue.shutdown()
        if (!taskQueue.awaitTermination(5, TimeUnit.SECONDS)) {
            taskQueue.shutdownNow()
        }
    }

    fun filter(value: Filter, view: FilterBoiView) {
        taskQueue.submit({

            if (filter == value) {
                return@submit
            }

            if (value.isEmpty) {
                filter = Filter("")
                curFilteredContent.clear()
                curFilteredContent.addAll(originalContent)
            } else {
                filterList(value)
            }

            Platform.runLater {
                setText(view)
            }
        })
    }

    private fun filterList(value: Filter) {
        val list = getList(value)
        filter = value
        doFiltering(filter, list)
    }

    private fun doFiltering(filter: Filter, list: MutableList<String>) {
        val start = System.currentTimeMillis()
        if (filter.regex) {
            val regx = Regex(filter.filterStr, RegexOption.IGNORE_CASE)
            list.removeIf {
                if (filter.not) {
                    it.matches(regx)
                } else {
                    !it.matches(regx)
                }
            }
        } else {
            list.removeIf {
                if (filter.not) {
                    it.contains(filter.filterStr, ignoreCase = true)
                } else {
                    !it.contains(filter.filterStr, ignoreCase = true)
                }
            }
        }
        lastSearchDur = "Searched in: ${System.currentTimeMillis() - start} ms"
    }

    private fun getList(newFilter: Filter): MutableList<String> {
        if (newFilter.not || newFilter.regex || newFilter.isNotSubFilter(filter)) {
            curFilteredContent.clear()
            curFilteredContent.addAll(originalContent)
        }
        return curFilteredContent
    }
}