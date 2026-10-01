package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.dto.ApiClientVO;
import com.yuchen.kami.entity.ApiClient;
import com.yuchen.kami.mapper.ApiClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiClientService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiClientMapper apiClientMapper;
    private final CryptoService cryptoService;

    public PageResult<ApiClientVO> page(int page, int size) {
        Page<ApiClient> result = apiClientMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ApiClient>().orderByDesc(ApiClient::getCreatedAt));
        List<ApiClientVO> vos = result.getRecords().stream()
                .map(c -> ApiClientVO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .appKey(c.getAppKey())
                        .appSecret(maskSecret(c.getAppSecret()))
                        .status(c.getStatus())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
        return new PageResult<>(vos, result.getTotal(), page, size);
    }

    public ApiClientVO create(String name) {
        String appKey = "YK" + HexFormat.of().formatHex(randomBytes(8)).toUpperCase();
        String plainSecret = cryptoService.generateApiSecret();

        ApiClient client = new ApiClient();
        client.setName(name);
        client.setAppKey(appKey);
        client.setAppSecret(cryptoService.sha256(plainSecret));
        client.setStatus(1);
        apiClientMapper.insert(client);

        return ApiClientVO.builder()
                .id(client.getId())
                .name(client.getName())
                .appKey(client.getAppKey())
                .appSecret(plainSecret)
                .status(client.getStatus())
                .createdAt(client.getCreatedAt())
                .build();
    }

    public void toggleStatus(Long id) {
        ApiClient client = apiClientMapper.selectById(id);
        if (client == null) {
            throw new BusinessException("客户端不存在");
        }
        client.setStatus(client.getStatus() == 1 ? 0 : 1);
        apiClientMapper.updateById(client);
    }

    public void delete(Long id) {
        apiClientMapper.deleteById(id);
    }

    private String maskSecret(String hashed) {
        if (hashed == null || hashed.length() < 8) {
            return "********";
        }
        return hashed.substring(0, 4) + "****" + hashed.substring(hashed.length() - 4);
    }

    private byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
