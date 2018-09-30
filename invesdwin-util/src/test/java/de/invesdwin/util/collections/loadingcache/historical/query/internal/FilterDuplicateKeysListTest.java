package de.invesdwin.util.collections.loadingcache.historical.query.internal;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.loadingcache.historical.IHistoricalEntry;
import de.invesdwin.util.collections.loadingcache.historical.ImmutableHistoricalEntry;
import de.invesdwin.util.time.fdate.FDateBuilder;

@NotThreadSafe
public class FilterDuplicateKeysListTest {

    private final List<IHistoricalEntry<Integer>> expectedList = new ArrayList<IHistoricalEntry<Integer>>() {
        {
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        }
    };
    private final List<IHistoricalEntry<Integer>> expectedListReverse = new ArrayList<IHistoricalEntry<Integer>>() {
        {
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
            add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        }
    };

    @Test
    public void testAdd() {
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedList);
    }

    @Test
    public void testAddReverse() {
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedListReverse);
    }

    @Test
    public void testAddAll() {
        final List<IHistoricalEntry<Integer>> input = new ArrayList<IHistoricalEntry<Integer>>();
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        //can not use addAll in that direction, also not needed right now
        list.addAll(input);
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedList);
    }

    @Test
    public void testAddAllReverse() {
        final List<IHistoricalEntry<Integer>> input = new ArrayList<IHistoricalEntry<Integer>>();
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        list.addAll(input);
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedListReverse);
    }

    @Test
    public void testAddAllReverseAfterAddFirst() {
        final List<IHistoricalEntry<Integer>> input = new ArrayList<IHistoricalEntry<Integer>>();
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        list.addAll(input);
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedListReverse);
    }

    @Test
    public void testAddAllReverseAfterAddLast() {
        final List<IHistoricalEntry<Integer>> input = new ArrayList<IHistoricalEntry<Integer>>();
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(2), 2));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(1), 1));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        input.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        final FilterDuplicateKeysList<Integer> list = new FilterDuplicateKeysList<Integer>(10);
        list.add(ImmutableHistoricalEntry.of(FDateBuilder.newDate(0), 0));
        list.addAll(0, input);
        Assertions.assertThat(list).hasSize(3);
        Assertions.assertThat(list).isEqualTo(expectedListReverse);
    }

}
