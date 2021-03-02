package com.blockinsight.basefi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Web3JUtils {

    /**
     * @param functionName     链上方法名
     * @param inputParameters  入参
     * @param outputParameters 返回值
     * @return
     */
    public static List<Type> getEthCallValue(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters, Web3j web3j, String from, String contractAddress) {
        try {
            Function function = new Function(
                    functionName,
                    inputParameters,
                    outputParameters
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(from, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST).sendAsync().get();
            return FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        } catch (Exception e) {
            log.error("获取链上数据异常", e);
            return new ArrayList<>();
        }
    }
}

