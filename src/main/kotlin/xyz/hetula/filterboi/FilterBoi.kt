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
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Tuomo Heino
 * @version 23.8.2017.
 */
class FilterBoi {
    private val taskQueue = Executors.newSingleThreadExecutor()
    private val originalContent: MutableList<String> = ArrayList()
    private val curFilteredContent: MutableList<String> = ArrayList()

    private var filter: Filter = Filter("")

    internal var lastSearchDur = 0L
        private set

    fun queryText(fromIndex: Int, length: Int, consumer: (line: String) -> Unit) {
        if (fromIndex >= curFilteredContent.size) {
            return
        }
        curFilteredContent.stream()
                .skip(fromIndex.toLong())
                .limit(length.toLong())
                .forEach { consumer(it) }
    }

    fun loadFile(file: Path, encoding: Charset, callback: () -> Unit) {
        taskQueue.submit {
            println("Reading Contents!")
            originalContent.clear()
            curFilteredContent.clear()
            readLines(file, encoding) {
                originalContent.add(it)
                println("Lines ${originalContent.size}")
            }
            println("Found ${originalContent.size} lines in log!")
            curFilteredContent.addAll(originalContent)
            doFiltering(filter, curFilteredContent)
            Platform.runLater {
                callback()
            }
        }
    }

    fun saveLog(toPath: Path) {
        val saveContent = ArrayList(curFilteredContent)
        taskQueue.submit {
            println("Saving to ${toPath.toAbsolutePath()}")
            Files.write(toPath, saveContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

    fun release() {
        taskQueue.shutdown()
        if (!taskQueue.awaitTermination(5, TimeUnit.SECONDS)) {
            taskQueue.shutdownNow()
        }
    }

    fun getCurrentLines(): Int {
        return curFilteredContent.size
    }

    fun filter(value: Filter, callback: () -> Unit) {
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
                callback()
            }
        })
    }

    private fun readLines(file: Path, encoding: Charset, consumer: (String) -> Unit) {
        if (!Files.exists(file)) {
            println("Path ${file.toAbsolutePath()} doesn't exist!")
            return
        }
        println("Reading contents of ${file.toAbsolutePath()}")
        val reader = Files.newBufferedReader(file, encoding)
        try {
            reader.lines().forEach { consumer(it) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            reader.close()
        }
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
        lastSearchDur = System.currentTimeMillis() - start
    }

    private fun getList(newFilter: Filter): MutableList<String> {
        if (newFilter.not || newFilter.regex || newFilter.isNotSubFilter(filter)) {
            curFilteredContent.clear()
            curFilteredContent.addAll(originalContent)
        }
        return curFilteredContent
    }
}