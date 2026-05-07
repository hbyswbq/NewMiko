package im.mingxi.mm.hook

import android.content.ContentValues
import com.highcapable.kavaref.KavaRef.Companion.resolve
import im.mingxi.miko.annotation.FunctionHookEntry
import im.mingxi.miko.hook.SwitchHook
import im.mingxi.miko.ui.util.FuncRouter
import im.mingxi.miko.util.Reflex
import im.mingxi.miko.util.dexkit.DexDesc
import im.mingxi.miko.util.dexkit.IFinder
import im.mingxi.mm.manager.impl.MMEnvManagerImpl
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Method

@FunctionHookEntry(itemType = FunctionHookEntry.WECHAT_ITEM)
class AutoReceiveTransferMoney : SwitchHook(), IFinder {
    override val name: String
        get() = "自动收款"
    override val uiItemLocation: String
        get() = FuncRouter.RED_PACKET

    private val onMsgInsertWithOnConflict: Method =
        Reflex.findMethod(
            Reflex.loadClass("com.tencent.wcdb.database.SQLiteDatabase")
        )
            .setMethodName("insertWithOnConflict")
            .get()

    override fun initOnce(): Boolean {
        onMsgInsertWithOnConflict.hookAfterIfEnable { param ->
            val contentValues = param.args[2] as ContentValues
            if (!contentValues.toString()
                    .contains("mmsupport-bin/readtemplate?")
            ) return@hookAfterIfEnable
            val xml = contentValues.getAsString("content")
            val talker = contentValues.getAsString("talker")
            val transcationId = extractTagContent(xml, "transcationid")!!
                .replace("<![CDATA[", "").replace("]]>", "")
            val transferId =
                extractTagContent(xml, "transferid")!!.replace("<![CDATA[", "").replace("]]>", "")
            val invalidTime =
                extractTagContent(xml, "invalidtime")!!.replace("<![CDATA[", "").replace("]]>", "")
            if (talker == MMEnvManagerImpl().getWxId()) return@hookAfterIfEnable
            // "transcationId:$transcationId, transferId:$transferId, invalidTime:$invalidTime, talker:$talker".d()
            val obj = TransferOperation.toMethod().declaringClass.resolve().firstConstructor {
                parameterCount(12)
            }.self.newInstance(
                transcationId,
                transferId,
                0,
                "confirm",
                talker,
                invalidTime.toInt(),
                "",
                null,
                1,
                null,
                0,
                talker
            )
            Reflex.findMethodObj(
                Reflex.findField(
                    NetSceneQueue.toMethod().declaringClass
                )
                    .setReturnType(
                        NetSceneQueue.toMethod().declaringClass
                    )
                    .get()
                    .get(null)
            )
                .setParamsLength(1)
                .setReturnType(Boolean::class.java)
                .get().invoke(
                    Reflex.findField(
                        NetSceneQueue.toMethod().declaringClass
                    )
                        .setReturnType(
                            NetSceneQueue.toMethod().declaringClass
                        )
                        .get()
                        .get(null),
                    obj
                )
        }

        return true
    }

    private val TransferOperation = DexDesc("$simpleTAG.Method.TransferOperation")
    private val NetSceneQueue: DexDesc =
        DexDesc("$simpleTAG.Method.NetSceneQueue")

    override fun dexFind(finder: DexKitBridge) {

        TransferOperation.findDexMethod(finder) {
            searchPackages("com.tencent.mm.plugin.remittance.model")
            matcher {
                usingStrings("/cgi-bin/mmpay-bin/transferoperation")
            }
        }

        NetSceneQueue.findDexMethod(finder) {
            searchPackages("com.tencent.mm.modelbase")

            matcher {
                usingStrings(
                    "MicroMsg.NetSceneQueue",
                    "doScene failed",
                    "reset::cancel scene",
                    "clearRunningQueue"
                )
            }

        }
    }

    fun extractTagContent(xml: String, tagName: String): String? {
        val regex = "<$tagName>(.*?)</$tagName>".toRegex(RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groups?.get(1)?.value
    }
}