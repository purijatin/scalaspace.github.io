package scalaspace

import google.maps._
import org.scalajs.dom
import org.scalajs.dom._
import upickle.default._

import scala.scalajs.js
import scala.scalajs.js.JSApp

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

object ScalaSpace extends JSApp {

  val infoWindow = new InfoWindow()

  def onClick(map: Map, marker: Marker, group: Group): Unit = {
    val link = Option(group.url)
//AIzaSyDGIJFLJjhYi2yZeCbZH9ieyuBIkT7v4pw
    link match {
      case Some(l) =>
        val content = document.createElement("a")
        content.setAttribute("href", l)
        content.innerHTML = group.name
        infoWindow.setContent(content)
        infoWindow.open(map, marker)
      case None =>
    }


  }

  def logo(group: Group): Icon =Icon("img/markers/parivartan.jpg")

  def initialize(): Unit = {
    console.log("starting...")

    val opts = MapOptions(
      center = new LatLng(17.439252, 78.372572),
      zoom = 13,
      mapTypeId = MapTypeId.ROADMAP,
      mapTypeControl = true,
      streetViewControl = true)

    val map = new Map(document.getElementById("map"), opts)

    ext.Ajax.get("data/groups.json").onSuccess {
      case request: XMLHttpRequest =>
        val markers = read[Groups](request.responseText).groups.map { group =>
          val marker = new Marker(MarkerOptions(
            position = new LatLng(group.latitude, group.longitude),
            icon = logo(group),
            map = map
          ))
          google.maps.event.addListener(marker, "click", () => onClick(map, marker, group))
          marker
        }
        // FIXME Restore the calculator function
        new MarkerClusterer(map, markers, js.Dynamic.literal(
          gridSize = 50,
          minimumClusterSize = 5
        ))
        if (dom.window.navigator.geolocation != null) {
          dom.window.navigator.geolocation.getCurrentPosition { (position: Position) =>
            map.setCenter(new LatLng(position.coords.latitude, position.coords.longitude))
          }
        }
    }

  }

  override def main(): Unit = {
    console.log("main..")
    google.maps.event.addDomListener(window, "load", () => initialize())
    val contribute = document.getElementById("contribute")
    document.getElementById("expand-contribute").addEventListener("click", { (event: Event) =>
      contribute.setAttribute("style", "display:block")
    })
    document.getElementById("collapse-contribute").addEventListener("click", { (event: Event) =>
      contribute.setAttribute("style", "display:none")
    })
  }

}
