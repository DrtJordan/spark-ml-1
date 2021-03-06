package com.drunk2013.spark.ml.fpg

import org.apache.spark.SparkFunSuite
import org.apache.spark.mllib.util.MLlibTestSparkContext

/**
  * Created by shuangfu on 17-1-23.
  */
class FPTreeSuite extends SparkFunSuite with MLlibTestSparkContext {
  test("添加 transaction item") {
    val tree = new FPTree[String]
      .add(Seq("a", "b", "c"))
      .add(Seq("a", "b", "y"))
      .add(Seq("b"))

    //测试FPTree树结构信息
    assert(tree.root.children.size == 2)
    assert(tree.root.children.contains("a"))
    assert(tree.root.children("a").item.equals("a"))
    assert(tree.root.children("a").count == 2)

    assert(tree.root.children.contains("b"))
    assert(tree.root.children("b").item.equals("b"))
    assert(tree.root.children("b").count == 1)

    var child = tree.root.children("a")
    assert(child.children.size == 1)
    assert(child.children.contains("b"))
    assert(child.children("b").item.equals("b"))
    assert(child.children("b").count == 2)

    child = child.children("b")
    assert(child.children.size == 2)
    assert(child.children.contains("c"))
    assert(child.children.contains("y"))
    assert(child.children("c").item.equals("c"))
    assert(child.children("y").item.equals("y"))
    assert(child.children("c").count == 1)
    assert(child.children("y").count == 1)
  }

  test("合并FPTree") {
    val tree1 = new FPTree[String]
      .add(Seq("a", "b", "c"))
      .add(Seq("a", "b", "y"))
      .add(Seq("b"))

    val tree2 = new FPTree[String]
      .add(Seq("a", "b"))
      .add(Seq("a", "b", "c"))
      .add(Seq("a", "b", "c", "d"))
      .add(Seq("a", "x"))
      .add(Seq("a", "x", "y"))
      .add(Seq("c", "n"))
      .add(Seq("c", "m"))

    val tree3 = tree1.merge(tree2)
    assert(tree3.root.children.size == 3)
    assert(tree3.root.children("a").count == 7)
    assert(tree3.root.children("b").count == 1)
    assert(tree3.root.children("c").count == 2)
    val child1 = tree3.root.children("a")
    assert(child1.children.size == 2)
    assert(child1.children("b").count == 5)
    assert(child1.children("x").count == 2)
    val child2 = child1.children("b")
    assert(child2.children.size == 2)
    assert(child2.children("y").count == 1)
    assert(child2.children("c").count == 3)
    val child3 = child2.children("c")
    assert(child3.children.size == 1)
    assert(child3.children("d").count == 1)
    val child4 = child1.children("x")
    assert(child4.children.size == 1)
    assert(child4.children("y").count == 1)
    val child5 = tree3.root.children("c")
    assert(child5.children.size == 2)
    assert(child5.children("n").count == 1)
    assert(child5.children("m").count == 1)

  }

  test("extract freq itemsets") {
    val tree = new FPTree[String]
      .add(Seq("a", "b", "c"))
      .add(Seq("a", "b", "y"))
      .add(Seq("a", "b"))
      .add(Seq("a"))
      .add(Seq("b"))
      .add(Seq("b", "n"))

    val freqItemsets = tree.extract(3L).map { case (items, count) =>
      (items.toSet, count)
    }.toSet
    val expected = Set(
      (Set("a"), 4L),
      (Set("b"), 5L),
      (Set("a", "b"), 3L)
    )
    assert(freqItemsets === expected)
  }


}
