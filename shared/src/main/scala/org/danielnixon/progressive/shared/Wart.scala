package org.danielnixon.progressive.shared

/**
  * Wart names for use with SuppressWarnings annotation.
  */
object Wart {
  final val Any = "org.wartremover.warts.Any"
  final val AsInstanceOf = "org.wartremover.warts.AsInstanceOf"
  final val Nothing = "org.wartremover.warts.Nothing"
  final val OptionPartial = "org.wartremover.warts.OptionPartial"
  final val Throw = "org.wartremover.warts.Throw"
}

/**
  * Wart names for use with SuppressWarnings annotation.
  */
object ExtraWart {
  final val UntypedEquality = "org.danielnixon.extrawarts.UntypedEquality"
}