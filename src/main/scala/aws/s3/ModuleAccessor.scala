package aws.s3

import java.io.File
import s3.S3Accessor
import awscala.s3.S3Object
import java.nio.file.{StandardCopyOption, CopyOption, Files}
import types.Module
import aws.UsesPrefix

class ModuleAccessor extends S3Accessor with UsesPrefix {
  implicit val const = ModuleAccessor

  ensureBucketExists(build(const.BUCKET_NAME))

  val bucket = s3.bucket(build(const.BUCKET_NAME)).get

  def getModuleExecutable(module: Module): Option[File] = {
    println("Trying to get: " + module.key)
    val s3Obj: Option[S3Object] = bucket.get(module.key)

    if (s3Obj.isDefined) {
      val file = File.createTempFile("module", module.key)

      Files.copy(s3Obj.get.content, file.toPath, StandardCopyOption.REPLACE_EXISTING)

      return Option.apply(file)
    } else {
      println("couldn't get it in S3")
      return Option.empty
    }
  }

  def putModule(module: Module, moduleExecutable: File): String = {
    return bucket.put(module.key, moduleExecutable).eTag
  }
}

object ModuleAccessor {
  val BUCKET_NAME: String = "modules"
}
