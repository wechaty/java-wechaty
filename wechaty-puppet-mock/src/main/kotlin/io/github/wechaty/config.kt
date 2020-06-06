package io.github.wechaty

import io.github.wechaty.filebox.FileBox

/**
 * config for mock
 * @author renxiaoya
 * @date 2020-06-02
 **/
const val CHATIE_OFFICIAL_ACCOUNT_QRCODE = "http://weixin.qq.com/r/qymXj7DEO_1ErfTs93y5"

fun qrCodeForChatie(): FileBox {
    return FileBox.fromQRCode(CHATIE_OFFICIAL_ACCOUNT_QRCODE)
}
