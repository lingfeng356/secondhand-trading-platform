package com.lingfeng.secondhandtradingplatform.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HotListCache {

    private Page<Product> page;
    private Long expiredTime;
}
