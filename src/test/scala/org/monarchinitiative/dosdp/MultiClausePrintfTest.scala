package org.monarchinitiative.dosdp

import zio.test.Assertion._
import zio.test._

object MultiClausePrintfTest extends DefaultRunnableSpec {

  def spec = suite("Print simple text")(
    test("Text should be replaced correctly") {
      val replacedText = PrintfText.replaced(Option("These cells also express %s."), Option(List("allen_makers_cat")), None, Option(Map("allen_makers_cat" -> SingleValue("my_value"))), true).getOrElse("")
      assert(replacedText)(equalTo("These cells also express 'my_value'."))
    },
    test("Multi_clause with single clause should replaced correctly") {
      val simple_clause = PrintfClause("These cells also express %s.", Option(List("allen_makers_cat")), None)
      val simple_printf = MultiClausePrintf(Option(" "), Option(List(simple_clause)))
      val replacedText = PrintfText.replaced(None, None, Option(simple_printf), Option(Map("allen_makers_cat" -> SingleValue("my_value"))), true).getOrElse("")
      assert(replacedText)(equalTo("These cells also express 'my_value'."))
    },
    test("Multi_clause with two clauses should replaced correctly") {
      val simple_clause1 = PrintfClause("These cells also express %s.", Option(List("allen_makers_cat")), None)
      val simple_clause2 = PrintfClause("These cells have projection type %s.", Option(List("projection_type")), None)
      val simple_printf = MultiClausePrintf(Option("_"), Option(List(simple_clause1, simple_clause2)))
      val replacedText = PrintfText.replaced(None, None, Option(simple_printf), Option(Map("allen_makers_cat" -> SingleValue("my_value"), "projection_type" -> SingleValue("my_value2"))), true).getOrElse("")
      assert(replacedText)(equalTo("These cells also express 'my_value'._These cells have projection type 'my_value2'."))
    },
    test("Multi_clause with three clauses, only two should replaced correctly") {
      val test = List().mkString("z")
      println("TEST-"+test+"-")
      val simple_clause1 = PrintfClause("These cells also express %s.", Option(List("allen_makers_cat")), None)
      val simple_clause2 = PrintfClause("These cells have projection type %s.", Option(List("not_existing_var")), None)
      val simple_clause3 = PrintfClause("These cells have some location %s.", Option(List("soma_location")), None)
      val simple_printf = MultiClausePrintf(Option("_"), Option(List(simple_clause1, simple_clause2, simple_clause3)))
      val replacedText = PrintfText.replaced(None, None, Option(simple_printf), Option(Map("allen_makers_cat" -> SingleValue("my_value"), "soma_location" -> SingleValue("my_value3"))), true).getOrElse("")
      assert(replacedText)(equalTo("These cells also express 'my_value'._These cells have some location 'my_value3'."))
    },
    test("Multi_clause with sub clauses should replaced together") {
      val sub_clause1 = PrintfClause("Sub text %s end.", Option(List("sub_var1")), None)
      val sub_clause2 = PrintfClause("Sub text2 %s end2.", Option(List("sub_var2")), None)
      val sub_clauses = MultiClausePrintf(Option(" "), Option(List(sub_clause1, sub_clause2)))

      val clause_with_sub = PrintfClause("These cells also express %s.", Option(List("allen_makers_cat")), Option(List(sub_clauses)))

      val top_printf = MultiClausePrintf(Option("_"), Option(List(clause_with_sub)))
      val replacedText = PrintfText.replaced(None, None, Option(top_printf), Option(Map(
        "allen_makers_cat" -> SingleValue("my_value"),
        "sub_var1" -> SingleValue("sub_value1"),
        "sub_var2" -> SingleValue("sub_value2"))), true).getOrElse("")

      assert(replacedText)(equalTo("These cells also express 'my_value'._Sub text 'sub_value1' end. Sub text2 'sub_value2' end2."))
    },
    test("Sub clauses ignored when parent fail") {
      val sub_clause1 = PrintfClause("Sub text %s end.", Option(List("sub_var1")), None)
      val sub_clause2 = PrintfClause("Sub text2 %s end2.", Option(List("sub_var2")), None)
      val sub_clauses = MultiClausePrintf(Option(" "), Option(List(sub_clause1, sub_clause2)))

      val clause_with_sub = PrintfClause("Top clause but fail %s.", Option(List("not existing")), Option(List(sub_clauses)))

      val top_printf = MultiClausePrintf(Option("_"), Option(List(clause_with_sub)))
      val replacedText = PrintfText.replaced(None, None, Option(top_printf), Option(Map(
        "sub_var1" -> SingleValue("sub_value1"),
        "sub_var2" -> SingleValue("sub_value2"))), true).getOrElse("")

      assert(replacedText)(equalTo(""))
    },
    test("Sub clauses ignored when parent fail, but siblings are ok") {
      val sub_clause1 = PrintfClause("Sub text %s end.", Option(List("sub_var1")), None)
      val sub_clause2 = PrintfClause("Sub text2 %s end2.", Option(List("sub_var2")), None)
      val sub_clauses = MultiClausePrintf(Option(" "), Option(List(sub_clause1, sub_clause2)))

      val clause_with_sub = PrintfClause("Second me but fail %s.", Option(List("not existing")), Option(List(sub_clauses)))
      val sibling_clause1 = PrintfClause("First me %s.", Option(List("sibling_var1")), None)
      val sibling_clause2 = PrintfClause("Third me %s.", Option(List("sibling_var3")), None)

      val top_printf = MultiClausePrintf(Option("_"), Option(List(sibling_clause1, clause_with_sub, sibling_clause2)))
      val replacedText = PrintfText.replaced(None, None, Option(top_printf), Option(Map(
        "sibling_var1" -> SingleValue("sibling_value1"),
        "sibling_var3" -> SingleValue("sibling_value3"),
        "sub_var1" -> SingleValue("sub_value1"),
        "sub_var2" -> SingleValue("sub_value2"))), true).getOrElse("")

      assert(replacedText)(equalTo("First me 'sibling_value1'._Third me 'sibling_value3'."))
    }
  )

}
