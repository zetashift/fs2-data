package fs2
package data
package cbor

import high._
import low._

import scodec.bits._

import cats.syntax.all._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFlatSpec with Matchers {

  def valuesMatcher(
      matcher: PartialFunction[List[CborValue], Boolean]): Either[List[CborValue], List[CborValue] => Boolean] =
    Right(vs => matcher.isDefinedAt(vs) && matcher(vs))

  val testCases = List[(Either[List[CborValue], List[CborValue] => Boolean], ByteVector)](
    List(CborValue.Integer(0l)).asLeft -> hex"00",
    List(CborValue.Integer(1l)).asLeft -> hex"01",
    List(CborValue.Integer(10l)).asLeft -> hex"0a",
    List(CborValue.Integer(23l)).asLeft -> hex"17",
    List(CborValue.Integer(24l)).asLeft -> hex"1818",
    List(CborValue.Integer(25l)).asLeft -> hex"1819",
    List(CborValue.Integer(100l)).asLeft -> hex"1864",
    List(CborValue.Integer(1000l)).asLeft -> hex"1903e8",
    List(CborValue.Integer(1000000l)).asLeft -> hex"1a000f4240",
    List(CborValue.Integer(1000000000000l)).asLeft -> hex"1b000000e8d4a51000",
    List(CborValue.Integer(-1l)).asLeft -> hex"20",
    List(CborValue.Integer(-10l)).asLeft -> hex"29",
    List(CborValue.Integer(-100l)).asLeft -> hex"3863",
    List(CborValue.Integer(-1000l)).asLeft -> hex"3903e7",
    List(CborValue.Float32(0.0f)).asLeft -> hex"f90000",
    List(CborValue.Float32(-0.0f)).asLeft -> hex"f98000",
    List(CborValue.Float32(1.0f)).asLeft -> hex"f93c00",
    List(CborValue.Float64(1.1d)).asLeft -> hex"fb3ff199999999999a",
    List(CborValue.Float32(1.5f)).asLeft -> hex"f93e00",
    List(CborValue.Float32(65504.0f)).asLeft -> hex"f97bff",
    List(CborValue.Float32(100000.0f)).asLeft -> hex"fa47c35000",
    List(CborValue.Float32(3.4028234663852886e+38f)).asLeft -> hex"fa7f7fffff",
    List(CborValue.Float64(1.0e+300)).asLeft -> hex"fb7e37e43c8800759c",
    List(CborValue.Float32(5.960464477539063e-8f)).asLeft -> hex"f90001",
    List(CborValue.Float32(0.00006103515625f)).asLeft -> hex"f90400",
    List(CborValue.Float32(-4.0f)).asLeft -> hex"f9c400",
    List(CborValue.Float64(-4.1)).asLeft -> hex"fbc010666666666666",
    List(CborValue.Float32(Float.NegativeInfinity)).asLeft -> hex"f97c00",
    valuesMatcher({ case List(CborValue.Float32(v)) => v.isNaN }) -> hex"f97e00",
    List(CborValue.Float32(Float.PositiveInfinity)).asLeft -> hex"fa7f800000",
    valuesMatcher({ case List(CborValue.Float32(v)) => v.isNaN }) -> hex"fa7fc00000",
    List(CborValue.Float32(Float.NegativeInfinity)).asLeft -> hex"faff800000",
    List(CborValue.Float64(Double.PositiveInfinity)).asLeft -> hex"fb7ff0000000000000",
    valuesMatcher({ case List(CborValue.Float64(v)) => v.isNaN }) -> hex"fb7ff8000000000000",
    List(CborValue.Float64(Double.NegativeInfinity)).asLeft -> hex"fbfff0000000000000",
    List(CborValue.False).asLeft -> hex"f4",
    List(CborValue.True).asLeft -> hex"f5",
    List(CborValue.Null).asLeft -> hex"f6",
    List(CborValue.Undefined).asLeft -> hex"f7",
    List(CborValue.SimpleValue(16)).asLeft -> hex"f0",
    List(CborValue.SimpleValue(255.toByte)).asLeft -> hex"f8ff",
    List(
      CborValue.Tagged(
        0,
        CborValue.TextString("2013-03-21T20:04:00Z"))).asLeft -> hex"c074323031332d30332d32315432303a30343a30305a",
    List(CborValue.Tagged(1, CborValue.Integer(1363896240))).asLeft -> hex"c11a514b67b0",
    List(CborValue.Tagged(1, CborValue.Float64(1363896240.5))).asLeft -> hex"c1fb41d452d9ec200000",
    List(CborValue.Tagged(23, CborValue.ByteString(hex"01020304"))).asLeft -> hex"d74401020304",
    List(CborValue.Tagged(24, CborValue.ByteString(hex"6449455446"))).asLeft -> hex"d818456449455446",
    List(
      CborValue.Tagged(32,
                       CborValue.TextString(
                         "http://www.example.com"))).asLeft -> hex"d82076687474703a2f2f7777772e6578616d706c652e636f6d",
    List(CborValue.ByteString(ByteVector.empty)).asLeft -> hex"40",
    List(CborValue.ByteString(hex"01020304")).asLeft -> hex"4401020304",
    List(CborValue.TextString("")).asLeft -> hex"60",
    List(CborValue.TextString("a")).asLeft -> hex"6161",
    List(CborValue.TextString("IETF")).asLeft -> hex"6449455446",
    List(CborValue.TextString("\"\\")).asLeft -> hex"62225c",
    List(CborValue.TextString("\u00fc")).asLeft -> hex"62c3bc",
    List(CborValue.TextString("\u6c34")).asLeft -> hex"63e6b0b4",
    List(CborValue.TextString("\ud800\udd51")).asLeft -> hex"64f0908591",
    List(CborValue.Array(Nil, false)).asLeft -> hex"80",
    List(
      CborValue.Array(List(CborValue.Integer(1), CborValue.Integer(2), CborValue.Integer(3)),
                      false)).asLeft -> hex"83010203",
    List(
      CborValue.Array(
        List(
          CborValue.Integer(1),
          CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), false),
          CborValue.Array(List(CborValue.Integer(4), CborValue.Integer(5)), false)
        ),
        false
      )).asLeft -> hex"8301820203820405",
    List(
      CborValue.Array(
        List(
          CborValue.Integer(1),
          CborValue.Integer(2),
          CborValue.Integer(3),
          CborValue.Integer(4),
          CborValue.Integer(5),
          CborValue.Integer(6),
          CborValue.Integer(7),
          CborValue.Integer(8),
          CborValue.Integer(9),
          CborValue.Integer(10),
          CborValue.Integer(11),
          CborValue.Integer(12),
          CborValue.Integer(13),
          CborValue.Integer(14),
          CborValue.Integer(15),
          CborValue.Integer(16),
          CborValue.Integer(17),
          CborValue.Integer(18),
          CborValue.Integer(19),
          CborValue.Integer(20),
          CborValue.Integer(21),
          CborValue.Integer(22),
          CborValue.Integer(23),
          CborValue.Integer(24),
          CborValue.Integer(25)
        ),
        false
      )).asLeft -> hex"98190102030405060708090a0b0c0d0e0f101112131415161718181819",
    List(CborValue.Map(Map.empty, false)).asLeft -> hex"a0",
    List(
      CborValue.Map(Map(CborValue.Integer(1) -> CborValue.Integer(2), CborValue.Integer(3) -> CborValue.Integer(4)),
                    false)).asLeft -> hex"a201020304",
    List(
      CborValue.Map(
        Map(CborValue.TextString("a") -> CborValue.Integer(1),
            CborValue.TextString("b") -> CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), false)),
        false
      )).asLeft -> hex"a26161016162820203",
    List(
      CborValue.Array(List(CborValue.TextString("a"),
                           CborValue.Map(Map(CborValue.TextString("b") -> CborValue.TextString("c")), false)),
                      false)).asLeft -> hex"826161a161626163",
    List(
      CborValue.Map(
        Map(
          CborValue.TextString("a") -> CborValue.TextString("A"),
          CborValue.TextString("b") -> CborValue.TextString("B"),
          CborValue.TextString("c") -> CborValue.TextString("C"),
          CborValue.TextString("d") -> CborValue.TextString("D"),
          CborValue.TextString("e") -> CborValue.TextString("E")
        ),
        false
      )).asLeft -> hex"a56161614161626142616361436164614461656145"
  )

  val streamingTestCases = List(
    (List(CborItem.StartIndefiniteByteString,
          CborItem.ByteString(hex"0102"),
          CborItem.ByteString(hex"030405"),
          CborItem.Break),
     List(CborValue.ByteString(hex"0102030405")),
     hex"5f42010243030405ff"),
    (List(CborItem.StartIndefiniteTextString,
          CborItem.TextString("strea"),
          CborItem.TextString("ming"),
          CborItem.Break),
     List(CborValue.TextString("streaming")),
     hex"7f657374726561646d696e67ff"),
    (List(CborItem.StartIndefiniteArray, CborItem.Break), List(CborValue.Array(Nil, true)), hex"9fff"),
    (List(
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"01"),
       CborItem.StartArray(2),
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"04"),
       CborItem.PositiveInt(hex"05"),
       CborItem.Break,
       CborItem.Break
     ),
     List(
       CborValue.Array(
         List(
           CborValue.Integer(1),
           CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), false),
           CborValue.Array(List(CborValue.Integer(4), CborValue.Integer(5)), true)
         ),
         true
       )),
     hex"9f018202039f0405ffff"),
    (List(
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"01"),
       CborItem.StartArray(2),
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.StartArray(2),
       CborItem.PositiveInt(hex"04"),
       CborItem.PositiveInt(hex"05"),
       CborItem.Break
     ),
     List(
       CborValue.Array(
         List(
           CborValue.Integer(1),
           CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), false),
           CborValue.Array(List(CborValue.Integer(4), CborValue.Integer(5)), false)
         ),
         true
       )),
     hex"9f01820203820405ff"),
    (List(
       CborItem.StartArray(3),
       CborItem.PositiveInt(hex"01"),
       CborItem.StartArray(2),
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"04"),
       CborItem.PositiveInt(hex"05"),
       CborItem.Break
     ),
     List(
       CborValue.Array(
         List(
           CborValue.Integer(1),
           CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), false),
           CborValue.Array(List(CborValue.Integer(4), CborValue.Integer(5)), true)
         ),
         false
       )),
     hex"83018202039f0405ff"),
    (List(
       CborItem.StartArray(3),
       CborItem.PositiveInt(hex"01"),
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.Break,
       CborItem.StartArray(2),
       CborItem.PositiveInt(hex"04"),
       CborItem.PositiveInt(hex"05")
     ),
     List(
       CborValue.Array(
         List(
           CborValue.Integer(1),
           CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), true),
           CborValue.Array(List(CborValue.Integer(4), CborValue.Integer(5)), false)
         ),
         false
       )),
     hex"83019f0203ff820405"),
    (List(
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"01"),
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.PositiveInt(hex"04"),
       CborItem.PositiveInt(hex"05"),
       CborItem.PositiveInt(hex"06"),
       CborItem.PositiveInt(hex"07"),
       CborItem.PositiveInt(hex"08"),
       CborItem.PositiveInt(hex"09"),
       CborItem.PositiveInt(hex"0a"),
       CborItem.PositiveInt(hex"0b"),
       CborItem.PositiveInt(hex"0c"),
       CborItem.PositiveInt(hex"0d"),
       CborItem.PositiveInt(hex"0e"),
       CborItem.PositiveInt(hex"0f"),
       CborItem.PositiveInt(hex"10"),
       CborItem.PositiveInt(hex"11"),
       CborItem.PositiveInt(hex"12"),
       CborItem.PositiveInt(hex"13"),
       CborItem.PositiveInt(hex"14"),
       CborItem.PositiveInt(hex"15"),
       CborItem.PositiveInt(hex"16"),
       CborItem.PositiveInt(hex"17"),
       CborItem.PositiveInt(hex"18"),
       CborItem.PositiveInt(hex"19"),
       CborItem.Break
     ),
     List(
       CborValue.Array(
         List(
           CborValue.Integer(1),
           CborValue.Integer(2),
           CborValue.Integer(3),
           CborValue.Integer(4),
           CborValue.Integer(5),
           CborValue.Integer(6),
           CborValue.Integer(7),
           CborValue.Integer(8),
           CborValue.Integer(9),
           CborValue.Integer(10),
           CborValue.Integer(11),
           CborValue.Integer(12),
           CborValue.Integer(13),
           CborValue.Integer(14),
           CborValue.Integer(15),
           CborValue.Integer(16),
           CborValue.Integer(17),
           CborValue.Integer(18),
           CborValue.Integer(19),
           CborValue.Integer(20),
           CborValue.Integer(21),
           CborValue.Integer(22),
           CborValue.Integer(23),
           CborValue.Integer(24),
           CborValue.Integer(25)
         ),
         true
       )),
     hex"9f0102030405060708090a0b0c0d0e0f101112131415161718181819ff"),
    (List(
       CborItem.StartIndefiniteMap,
       CborItem.TextString("a"),
       CborItem.PositiveInt(hex"01"),
       CborItem.TextString("b"),
       CborItem.StartIndefiniteArray,
       CborItem.PositiveInt(hex"02"),
       CborItem.PositiveInt(hex"03"),
       CborItem.Break,
       CborItem.Break
     ),
     List(
       CborValue.Map(
         Map(CborValue.TextString("a") -> CborValue.Integer(1),
             CborValue.TextString("b") -> CborValue.Array(List(CborValue.Integer(2), CborValue.Integer(3)), true)),
         true
       )),
     hex"bf61610161629f0203ffff"),
    (List(CborItem.StartArray(2),
          CborItem.TextString("a"),
          CborItem.StartIndefiniteMap,
          CborItem.TextString("b"),
          CborItem.TextString("c"),
          CborItem.Break),
     List(
       CborValue.Array(List(CborValue.TextString("a"),
                            CborValue.Map(Map(CborValue.TextString("b") -> CborValue.TextString("c")), true)),
                       false)),
     hex"826161bf61626163ff"),
    (List(
       CborItem.StartIndefiniteMap,
       CborItem.TextString("Fun"),
       CborItem.True,
       CborItem.TextString("Amt"),
       CborItem.NegativeInt(hex"01"),
       CborItem.Break
     ),
     List(
       CborValue.Map(Map(CborValue.TextString("Fun") -> CborValue.True,
                         CborValue.TextString("Amt") -> CborValue.Integer(-2)),
                     true)),
     hex"bf6346756ef563416d7421ff")
  )

  for ((expected, input) <- testCases) {
    "CBOR value parser" should s"parse ${input.toHex} properly" in {
      val result = Stream.chunk(Chunk.byteVector(input)).through(values[Fallible]).compile.toList
      expected match {
        case Left(expected) =>
          result shouldBe Right(expected)
        case Right(f) =>
          result.map(f) shouldBe Right(true)
      }
    }
  }

  for ((expectedLow, expectedHigh, input) <- streamingTestCases) {
    "CBOR value parser" should s"parse ${input.toHex} properly" in {
      val bytes = Stream.chunk(Chunk.byteVector(input))
      val low = bytes.through(items[Fallible]).compile.toList
      val high = bytes.through(values[Fallible]).compile.toList
      low shouldBe Right(expectedLow)
      high shouldBe Right(expectedHigh)
    }
  }

  for ((_, input) <- testCases) {
    "CBOR parsing/serializing" should s"be fix point for ${input.toHex}" in {
      val roundtrip = Stream
        .chunk(Chunk.byteVector(input))
        .through(items[Fallible])
        .through(low.toBinary)
        .compile
        .to(Chunk)
        .map(_.toByteVector)

      roundtrip shouldBe Right(input)
    }
  }

  for ((_, _, input) <- streamingTestCases) {
    "CBOR parsing/serializing" should s"be fix point for ${input.toHex}" in {
      val roundtrip = Stream
        .chunk(Chunk.byteVector(input))
        .through(items[Fallible])
        .through(low.toBinary)
        .compile
        .to(Chunk)
        .map(_.toByteVector)

      roundtrip shouldBe Right(input)
    }
  }

}
