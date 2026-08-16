# CoralDS
Garbage-free, single-threaded data-structures (maps, lists and sets) optimized for speed.

## Features

- Designed to avoid garbage during normal use by reusing iterators, entries and internal storage.
- Built for fast, single-threaded code. The data structures are not thread-safe and must not be accessed concurrently.
- Null keys, values and elements are not accepted.
- Primitive-specialized collections avoid boxing for `byte`, `char`, `int` and `long` keys or elements.
- Familiar operations cover adding, finding, removing, clearing and iterating over data.
- Capacities and load factors can be configured for collections that use hash tables or resizable storage.

## Maps

- `Map` — A general-purpose hash map that compares keys with `equals()` and `hashCode()`.
- `LinkedMap` — A hash map that iterates over entries in insertion order.
- `CharMap` — A fixed-size map with constant-time access for ASCII `char` keys.
- `ByteMap` — A fixed-size map with constant-time access across the full range of `byte` keys.
- `IntMap` — A hash map with primitive `int` keys, avoiding key boxing.
- `LongMap` — A hash map with primitive `long` keys, avoiding key boxing.
- `ByteBufferMap` — A hash map for variable-length `ByteBuffer` or byte-array keys up to a configured maximum length.
- `CharSequenceMap` — A hash map for variable-length `CharSequence` keys up to a configured maximum length.
- `IdentityMap` — A hash map that compares keys by reference identity (`==`) instead of `equals()`.

## Lists

- `ArrayList` — A resizable, array-backed list for objects with fast indexed access.
- `ArrayLinkedList` — A hybrid list that fills a fixed-size array first and uses a linked list for overflow.
- `IntArrayList` — A resizable, array-backed list for primitive `int` values.
- `LongArrayList` — A resizable, array-backed list for primitive `long` values.
- `LinkedList` — A garbage-free doubly linked list for objects.
- `IntLinkedList` — A garbage-free doubly linked list for primitive `int` values.
- `LongLinkedList` — A garbage-free doubly linked list for primitive `long` values.

## Sets

- `Set` — A hash-based collection of unique objects compared with `equals()` and `hashCode()`.
- `LinkedSet` — A set that keeps unique objects in insertion order.
- `IntSet` — A hash-based set for unique primitive `int` values.
- `LongSet` — A hash-based set for unique primitive `long` values.
- `IdentitySet` — A set that compares objects by reference identity (`==`) instead of `equals()`.
