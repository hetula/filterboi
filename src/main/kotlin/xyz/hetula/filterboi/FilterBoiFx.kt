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
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

/**
 * @author Tuomo Heino
 * @version 23.8.2017.
 */
class FilterBoiFx : Application() {

    override fun start(primaryStage: Stage?) {
        primaryStage!!

        val fxmlLoader = FXMLLoader()
        val root = fxmlLoader.load<BorderPane>(javaClass.getResourceAsStream("/boi_gui.fxml"))
        val controller = fxmlLoader.getController<FilterBoiView>()
        val window = createWindow(primaryStage)

        controller.window = window
        controller.presenter = FilterBoiPresenter(controller, window)

        val scene = Scene(root, 1024.0, 768.0)
        scene.stylesheets.add(javaClass.getResource("/boi.css").toExternalForm())

        primaryStage.setOnHidden { controller.presenter.detach() }

        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun createWindow(stage: Stage): FilterBoiContract.Window {
        return object : FilterBoiContract.Window {
            override fun setTitle(title: String) {
                stage.title = title
            }

            override fun getPrimaryStage(): Stage {
                return stage
            }
        }
    }

}