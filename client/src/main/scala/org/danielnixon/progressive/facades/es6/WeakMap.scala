package org.danielnixon.progressive.facades.es6

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSGlobal

/**
  * Facade for WeakMap.
  * @see https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/WeakMap
  */
@js.native
@JSGlobal
class WeakMap[K <: js.Any, V <: js.Any] extends js.Object {

  def delete(key: K): Unit = js.native

  def has(key: K): Boolean = js.native

  def get(key: K): UndefOr[V] = js.native

  def set(key: K, value: V): Unit = js.native

}
