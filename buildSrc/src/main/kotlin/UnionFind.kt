/**
 * Pointer-based implementation of [Union-Find](https://en.wikipedia.org/wiki/Disjoint-set_data_structure) with path compression
 * that also allows associating a single value with each set.
 */
class UnionFind<K : Any, V : Any>(
	private val merge: ((canonical: V, alternative: V) -> V)? = null
) {
	private val nodes = mutableMapOf<K, Node<K, V>>()

	/**
	 * Find a node matching a given key, if none is found, put and returns an empty node.
	 */
	private fun _find(key: K): Node<K, V> = nodes.getOrPut(key) { Node(key, null, null) }.find()

	fun union(canonical: K, alternative: K) {
		val canonicalRoot = _find(canonical)
		val alternativeRoot = _find(alternative)
		if (canonicalRoot != alternativeRoot) {
			if (alternativeRoot.value != null) {
				if (canonicalRoot.value != null) {
					if (merge != null) {
						canonicalRoot.value = merge!!(canonicalRoot.value!!, alternativeRoot.value!!)
						alternativeRoot.value = null
					} else if (canonicalRoot.value == alternativeRoot.value) {
						alternativeRoot.value = null
					} else {
						throw IllegalStateException("Canonical and alternative roots have different values: ${canonicalRoot.value} and ${alternativeRoot.value}")
					}
				} else {
					canonicalRoot.value = alternativeRoot.value
					alternativeRoot.value = null
				}
			}
			alternativeRoot.parent = canonicalRoot
		}
	}

	/**
	 * Given a key, return the key of the node matching the given key.
	 */
	fun find(key: K): K = _find(key).key

	operator fun get(key: K): V? = _find(key).value
	operator fun set(key: K, value: V) {
		_find(key).value = value
	}

	/**
	 * Given a key, perform a remapping of the nodes value, returning the new value.
	 *
	 * @return new value of the node.
	 */
	fun compute(key: K, remappingFunction: (K, V?) -> V?): V? {
		// Find the node
		val node = _find(key)

		// Perform a remapping of its current value
		node.value = remappingFunction(node.key, node.value)

		// Return the new value
		return node.value
	}

	fun asSequence() = sequence {
		for (node in nodes.values) {
			if (node.parent == node) yield(node.key to node.value)
		}
	}

	private data class Node<K : Any, V : Any>(val key: K, var parent: Node<K, V>?, var value: V?) {
		init {
			parent = parent ?: this
		}

		fun find(): Node<K, V> {
			if (parent != this) parent = simpleFind() // Path compression
			return parent!!
		}

		private fun simpleFind(): Node<K, V> = when {
			parent != this -> parent!!.find()
			else -> this
		}
	}
}
