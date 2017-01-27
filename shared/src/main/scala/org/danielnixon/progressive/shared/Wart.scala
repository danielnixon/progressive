package org.danielnixon.progressive.shared

/**
  * Wart names for use with SuppressWarnings annotation.
  */
object Wart {
  final val Any = "org.wartremover.warts.Any"
  final val AsInstanceOf = "org.wartremover.warts.AsInstanceOf"
  final val ExplicitImplicitTypes = "org.wartremover.warts.ExplicitImplicitTypes"
  final val Nothing = "org.wartremover.warts.Nothing"
  final val OptionPartial = "org.wartremover.warts.OptionPartial"
  final val Throw = "org.wartremover.warts.Throw"
  final val ToString = "org.wartremover.warts.ToString"

  final val StringOpsPartial = "org.danielnixon.playwarts.StringOpsPartial"
  final val TraversableOnceOps = "org.danielnixon.playwarts.TraversableOnceOps"
  final val WSResponsePartial = "org.danielnixon.playwarts.WSResponsePartial"
}
