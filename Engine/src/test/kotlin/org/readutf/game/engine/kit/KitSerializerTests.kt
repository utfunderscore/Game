package org.readutf.game.engine.kit

class KitSerializerTests {
//    @Test
//    fun testPalletWriteRead() {
//        val byteArray = ByteArrayOutputStream()
//
//        val inputItems =
//            listOf(
//                ItemStack.of(Material.STONE),
//                ItemStack.of(Material.STONE, 2),
//                ItemStack.of(Material.STONE, 3),
//            )
//        PalletKitSerializer.writeItemStacks(
//            byteArray,
//            inputItems,
//        )
//
//        val inputStream = byteArray.toByteArray().inputStream()
//
//        val outputItems = PalletKitSerializer.readItemStacks(inputStream)
//
//        assertEquals(inputItems, outputItems)
//    }
//
//    @Test
//    fun testKitSerialization() {
//        val kit =
//            Kit(
//                listOf(
//                    ItemStack.of(Material.STONE),
//                    ItemStack.of(Material.STONE, 2),
//                    ItemStack.of(Material.STONE, 3),
//                ),
//                listOf(
//                    ItemStack.of(Material.DIAMOND_BOOTS),
//                    ItemStack.of(Material.DIAMOND_LEGGINGS),
//                    ItemStack.of(Material.DIAMOND_CHESTPLATE),
//                    ItemStack.of(Material.DIAMOND_HELMET),
//                ),
//            )
//
//        val outputStream = ByteArrayOutputStream()
//
//        PalletKitSerializer.serialize(kit, outputStream)
//        val deserializedKit = PalletKitSerializer.deserialize(ByteArrayInputStream(outputStream.toByteArray()))
//
//        assertEquals(true, kit.items.size == deserializedKit.items.size)
//    }
}
