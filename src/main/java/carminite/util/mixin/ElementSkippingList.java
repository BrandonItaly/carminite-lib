package carminite.util.mixin;

import org.jspecify.annotations.NonNull;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class ElementSkippingList<T> extends AbstractList<T> {
	private final List<T> delegate;
	private final Predicate<T> handled;
	private boolean iterated;

	public ElementSkippingList(List<T> delegate, Predicate<T> handled) {
		this.delegate = delegate;
		this.handled = handled;
	}

	@Override
	public @NonNull Iterator<T> iterator() {
		if (iterated)
			throw new IllegalStateException("ElementSkippingList is single-pass");
		iterated = true;
		Iterator<T> it = delegate.iterator();
		return new Iterator<>() {
			private T pending;
			private boolean hasPending;

			@Override
			public boolean hasNext() {
				while (!hasPending && it.hasNext()) {
					T element = it.next();
					if (handled.test(element))
						continue;
					pending = element;
					hasPending = true;
				}
				return hasPending;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				T result = pending;
				pending = null;
				hasPending = false;
				return result;
			}
		};
	}

	@Override
	public T get(int index) {
		throw new UnsupportedOperationException("Only iterator() is supported");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Only iterator() is supported");
	}
}
