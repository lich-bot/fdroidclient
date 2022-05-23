package org.fdroid.index.v2

import com.goncalossilva.resources.Resource
import org.fdroid.index.IndexParser.parseV2
import org.fdroid.test.TestDataEmptyV2
import org.fdroid.test.TestDataMaxV2
import org.fdroid.test.TestDataMidV2
import org.fdroid.test.TestDataMinV2
import kotlin.test.Test
import kotlin.test.assertEquals

internal class IndexV2Test {

    @Test
    fun testEmpty() {
        testIndexEquality("src/sharedTest/resources/index-empty-v2.json", TestDataEmptyV2.index)
    }

    @Test
    fun testMin() {
        testIndexEquality("src/sharedTest/resources/index-min-v2.json", TestDataMinV2.index)
    }

    @Test
    fun testMinReordered() {
        testIndexEquality("src/sharedTest/resources/index-min-reordered-v2.json",
            TestDataMinV2.index)
    }

    @Test
    fun testMid() {
        testIndexEquality("src/sharedTest/resources/index-mid-v2.json", TestDataMidV2.index)
    }

    @Test
    fun testMax() {
        testIndexEquality("src/sharedTest/resources/index-max-v2.json", TestDataMaxV2.index)
    }

    private fun testIndexEquality(file: String, expectedIndex: IndexV2) {
        val indexV2Res = Resource(file)
        val indexV2Str = indexV2Res.readText()
        val indexV2 = parseV2(indexV2Str)

        assertEquals(expectedIndex, indexV2)
    }

}