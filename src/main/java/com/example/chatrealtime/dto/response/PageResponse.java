package com.example.chatrealtime.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
        // dữ liệu
    private List<T> content;

    // số lượng request
    private int size;

    // số item trả về thực tế
    private int returned;

    // cursor hiện tại (điểm bắt đầu của request)
    private Object cursor;

    // cursor tiếp theo để load thêm
    private Object nextCursor;

    // còn dữ liệu không
    private boolean hasMore;
}
