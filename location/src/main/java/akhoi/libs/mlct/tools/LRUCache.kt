package akhoi.libs.mlct.tools

class LRUCache<K, V>(private val capacity: Int) {
    private val cache = HashMap<K, Node<K, V>>()
    private val head = Node<K, V>(null, null)
    private val tail = Node<K, V>(null, null)

    init {
        head.next = tail
        tail.prev = head
    }

    @Synchronized
    operator fun get(key: K): V? {
        val node = cache[key]
        return if (node != null) {
            moveToHead(node)
            node.value
        } else {
            null
        }
    }

    @Synchronized
    operator fun set(key: K, value: V) {
        val existingNode = cache[key]

        if (existingNode != null) {
            existingNode.value = value
            moveToHead(existingNode)
        } else {
            if (cache.size >= capacity) {
                val lastNode = removeTail()
                cache.remove(lastNode?.key)
            }

            val newNode = Node(key, value)
            cache[key] = newNode
            addToHead(newNode)
        }
    }

    @Synchronized
    fun remove(key: K): V? {
        val node = cache[key]
        return if (node != null) {
            cache.remove(key)
            removeNode(node)
            node.value
        } else {
            null
        }
    }

    @Synchronized
    fun size(): Int = cache.size

    @Synchronized
    fun clear() {
        cache.clear()
        head.next = tail
        tail.prev = head
    }

    private fun addToHead(node: Node<K, V>) {
        node.prev = head
        node.next = head.next
        head.next?.prev = node
        head.next = node
    }

    private fun removeNode(node: Node<K, V>) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
    }

    private fun moveToHead(node: Node<K, V>) {
        removeNode(node)
        addToHead(node)
    }

    private fun removeTail(): Node<K, V>? {
        val lastNode = tail.prev
            ?: return null
        removeNode(lastNode)
        return lastNode
    }

    private data class Node<K, V>(
        val key: K?,
        var value: V?,
        var prev: Node<K, V>? = null,
        var next: Node<K, V>? = null
    )
}