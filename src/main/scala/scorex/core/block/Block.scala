package scorex.core.block

import io.circe.Json
import scorex.core.{NodeViewModifier, PersistentNodeViewModifier}
import scorex.core.block.Block.{Timestamp, Version}
import scorex.core.consensus.History
import scorex.crypto.encode.Base58
import scorex.core.serialization.{JsonSerializable, Serializer}
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction._
import shapeless.HList

/**
  * A block is an atomic piece of data network participates are agreed on.
  *
  * A block has:
  * - transactional data: a sequence of transactions, where a transaction is an atomic state update.
  * Some metadata is possible as well(transactions Merkle tree root, state Merkle tree root etc).
  *
  * - consensus data to check whether block was generated by a right party in a right way. E.g.
  * "baseTarget" & "generatorSignature" fields in the Nxt block structure, nonce & difficulty in the
  * Bitcoin block structure.
  *
  * - a signature(s) of a block generator(s)
  *
  * - additional data: block structure version no, timestamp etc
  */

trait Block[P <: Proposition, TX <: Transaction[P]]
  extends PersistentNodeViewModifier[P, TX] with JsonSerializable {

  type BlockFields <: HList

  def version: Version

  def parentId: NodeViewModifier.ModifierId

  def encodedId: String = Base58.encode(id)

  def json: Json

  def timestamp: Timestamp

  def blockFields: BlockFields
}

object Block {
  type BlockId = NodeViewModifier.ModifierId
  type Timestamp = Long
  type Version = Byte

  val BlockIdLength: Int = NodeViewModifier.ModifierIdSize

}

trait BlockCompanion[P <: Proposition, TX <: Transaction[P], B <: Block[P, TX]]
  extends Serializer[B] {

  def isValid(block: B): Boolean

  def build(blockFields: B#BlockFields): B

  /**
    * Get block producers(miners/forgers). Usually one miner produces a block, but in some proposals not
    * (see e.g. Proof-of-Activity paper of Bentov et al. http://eprint.iacr.org/2014/452.pdf)
    *
    * @return blocks' producers
    */
  def producers(block: B, history: History[P, TX, B, _, _]): Seq[P]
}