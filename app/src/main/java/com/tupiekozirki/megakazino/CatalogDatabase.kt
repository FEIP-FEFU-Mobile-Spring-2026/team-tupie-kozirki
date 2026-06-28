package com.tupiekozirki.megakazino

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortDescription: String,
    val longDescription: String,
    val priceInKopecks: Long,
    val imageUrl: String,
    val tagsJson: String,
    val sizesJson: String,
    val categoryId: String,
    val material: String,
    val weight: String,
    val season: String,
    val countryOfOrigin: String,
)

@Dao
interface CatalogDao {
    @Query("SELECT * FROM categories")
    fun getCategories(): List<CategoryEntity>

    @Query("SELECT * FROM products")
    fun getProducts(): List<ProductEntity>

    @Query("DELETE FROM categories")
    fun clearCategories()

    @Query("DELETE FROM products")
    fun clearProducts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>)

    @Transaction
    fun replaceCatalog(
        categories: List<CategoryEntity>,
        products: List<ProductEntity>,
    ) {
        clearCategories()
        clearProducts()
        insertCategories(categories)
        insertProducts(products)
    }
}

@Database(
    entities = [CategoryEntity::class, ProductEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao

    companion object {
        @Volatile
        private var instance: CatalogDatabase? = null

        fun getInstance(context: Context): CatalogDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CatalogDatabase::class.java,
                    "catalog.db",
                ).build().also { instance = it }
            }
        }
    }
}

private val gson = Gson()
private val sizeListType = object : TypeToken<List<Size>>() {}.type
private val stringListType = object : TypeToken<List<String>>() {}.type

fun CatalogResponse.toCategoryEntities(): List<CategoryEntity> {
    return categories.map { category ->
        CategoryEntity(
            id = category.id,
            name = category.name,
        )
    }
}

fun CatalogResponse.toProductEntities(): List<ProductEntity> {
    return items.map { product ->
        ProductEntity(
            id = product.id,
            name = product.name,
            shortDescription = product.shortDescription,
            longDescription = product.longDescription,
            priceInKopecks = product.priceInKopecks,
            imageUrl = product.imageUrl,
            tagsJson = gson.toJson(product.tags),
            sizesJson = gson.toJson(product.sizes),
            categoryId = product.categoryId,
            material = product.material,
            weight = product.weight,
            season = product.season,
            countryOfOrigin = product.countryOfOrigin,
        )
    }
}

fun List<CategoryEntity>.toCategories(): List<Category> {
    return map { entity ->
        Category(
            id = entity.id,
            name = entity.name,
        )
    }
}

fun List<ProductEntity>.toProducts(): List<Product> {
    return map { entity ->
        Product(
            id = entity.id,
            name = entity.name,
            shortDescription = entity.shortDescription,
            longDescription = entity.longDescription,
            priceInKopecks = entity.priceInKopecks,
            imageUrl = entity.imageUrl,
            tags = gson.fromJson(entity.tagsJson, stringListType),
            sizes = gson.fromJson(entity.sizesJson, sizeListType),
            categoryId = entity.categoryId,
            material = entity.material,
            weight = entity.weight,
            season = entity.season,
            countryOfOrigin = entity.countryOfOrigin,
        )
    }
}
