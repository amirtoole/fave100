// Automatically Generated -- DO NOT EDIT
// com.fave100.client.requestfactory.ApplicationRequestFactory
package com.fave100.client.requestfactory;
import java.util.Arrays;
import com.google.web.bindery.requestfactory.vm.impl.OperationData;
import com.google.web.bindery.requestfactory.vm.impl.OperationKey;
public final class ApplicationRequestFactoryDeobfuscatorBuilder extends com.google.web.bindery.requestfactory.vm.impl.Deobfuscator.Builder {
{
withOperation(new OperationKey("ivJOvWAq6$29ItJ4N4qK878smig="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/InstanceRequest;")
  .withDomainMethodDescriptor("()V")
  .withMethodName("remove")
  .withRequestContext("com.fave100.client.requestfactory.FaveItemRequest")
  .build());
withOperation(new OperationKey("6utjAQR3HGGVtfCaNTL$dvhbxQ8="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/InstanceRequest;")
  .withDomainMethodDescriptor("()Lcom/fave100/server/domain/FaveItem;")
  .withMethodName("persist")
  .withRequestContext("com.fave100.client.requestfactory.FaveItemRequest")
  .build());
withOperation(new OperationKey("df0DKwCbgaZ5Y84jFFa42wbClVY="),
  new OperationData.Builder()
  .withClientMethodDescriptor("()Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("()Ljava/util/List;")
  .withMethodName("getAllFaveItemsForUser")
  .withRequestContext("com.fave100.client.requestfactory.FaveItemRequest")
  .build());
withOperation(new OperationKey("7p63a6sAZB$VlRSF$vbfhBf6eLk="),
  new OperationData.Builder()
  .withClientMethodDescriptor("(Ljava/lang/Long;)Lcom/google/web/bindery/requestfactory/shared/Request;")
  .withDomainMethodDescriptor("(Ljava/lang/Long;)Lcom/fave100/server/domain/FaveItem;")
  .withMethodName("findFaveItem")
  .withRequestContext("com.fave100.client.requestfactory.FaveItemRequest")
  .build());
withRawTypeToken("82QpBIZ2Thda3j4h9ELHeAmdfBE=", "com.fave100.client.requestfactory.FaveItemProxy");
withRawTypeToken("w1Qg$YHpDaNcHrR5HZ$23y518nA=", "com.google.web.bindery.requestfactory.shared.EntityProxy");
withRawTypeToken("FXHD5YU0TiUl3uBaepdkYaowx9k=", "com.google.web.bindery.requestfactory.shared.BaseProxy");
withClientToDomainMappings("com.fave100.server.domain.FaveItem", Arrays.asList("com.fave100.client.requestfactory.FaveItemProxy"));
}}
