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

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import java.io.File

/**
 * @author Tuomo Heino
 * @version 23.8.2017.
 */
class FilterBoiFx : Application() {
    override fun start(primaryStage: Stage?) {
        primaryStage!!
        val selChooser = FileChooser()
        selChooser.title = "Select log file"
        selChooser.initialDirectory = File(System.getProperty("user.home"))

        val searchField = TextField()
        searchField.promptText = "Search..."
        searchField.prefColumnCount = 24

        val notSearch = CheckBox("Not matching")
        val regexSearch = CheckBox("Regex")

        val emptySpace = Region()
        HBox.setHgrow(emptySpace, Priority.ALWAYS)

        val loadBtn = Button("Load File...")

        val horiTop = HBox(5.0, searchField, notSearch, regexSearch, emptySpace, loadBtn)
        horiTop.alignment = Pos.CENTER_LEFT
        horiTop.padding = Insets(4.0)

        val logArea = CodeArea()
        logArea.isEditable = false
        logArea.isWrapText = false
        logArea.paragraphGraphicFactory = LineNumberFactory.get(logArea)

        val scroller = ScrollPane(logArea)
        scroller.isFitToWidth = true
        scroller.isFitToHeight = true

        val toolbarField = Label()
        val toolbar = ToolBar(toolbarField)

        val root = BorderPane(scroller)
        root.top = horiTop
        root.bottom = toolbar

        val view = createView(logArea, toolbarField)

        FilterBoi.setText(view)

        loadBtn.setOnAction {
            val f = selChooser.showOpenDialog(primaryStage) ?: return@setOnAction
            FilterBoi.loadFile(f.toPath(), view)
            primaryStage.title = "${f.absolutePath} - Filter Boi"
        }

        notSearch.setOnAction {
            FilterBoi.filter(Filter(searchField.text, notSearch.isSelected, regexSearch.isSelected), view)
        }

        regexSearch.setOnAction {
            FilterBoi.filter(Filter(searchField.text, notSearch.isSelected, regexSearch.isSelected), view)
        }

        searchField.textProperty().addListener { _, _, newValue ->
            FilterBoi.filter(Filter(newValue, notSearch.isSelected, regexSearch.isSelected), view)
        }

        primaryStage.scene = Scene(root, 1024.0, 768.0)
        primaryStage.title = "Filter Boi"
        primaryStage.show()
    }

    private fun createView(codeArea: CodeArea, toolbar: Label): FilterBoiView = object : FilterBoiView {
        override fun appendText(line: String) {
            codeArea.appendText(line)
        }

        override fun clearText() {
            codeArea.clear()
        }

        override fun setToolbarText(string: String) {
            toolbar.text = string
        }
    }

}