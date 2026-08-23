package com.akydd.realworld_spring.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A {@link Pageable} for RealWorld's raw {@code limit}/{@code offset} paging (Spring Data's
 * {@code PageRequest} is page-number based, which only maps cleanly when offset is a multiple of
 * limit). This supports an arbitrary offset. Sorting is left to the query's {@code order by}.
 */
public final class OffsetPageable implements Pageable {

    private final int offset;
    private final int limit;

    public OffsetPageable(int offset, int limit) {
        this.offset = Math.max(0, offset);
        this.limit = Math.max(1, limit);
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetPageable(offset + limit, limit);
    }

    @Override
    public Pageable previousOrFirst() {
        return new OffsetPageable(Math.max(0, offset - limit), limit);
    }

    @Override
    public Pageable first() {
        return new OffsetPageable(0, limit);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetPageable(pageNumber * limit, limit);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
