package dynamo

import dynamo.JobStatusAccessor
import org.scalatest.FlatSpec
import awscala.{Region, CredentialsLoader}
import helper.RequiresAWS
import types.JobInfo
import awscala.dynamodbv2.{Table, DynamoDB}
import aws.UsesPrefix
import constants.JobStatusTableConstants

class JobStatusAccessorTest extends FlatSpec with RequiresAWS with UsesPrefix {
  implicit val dynamo = DynamoDB.at(Region.Oregon)
  implicit val const = JobStatusTableConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def withJob(testCode: String => Any) {
    val ji = new JobInfo {
      attempt = 0
      channel = "TestChannel"
      channelVersion = 1
      farmId = "Somefarm"
      module = "a module"
      moduleVersion = 5
    }

    val jsa = new JobStatusAccessor

    val id = jsa.addEntry(ji)

    try {
      testCode(id)
    }
    finally dynamo.deleteItem(table, id)
  }

  "creating two identical jobs" should "have unique IDs" in withJob { id1 =>
    withJob { id2 =>
      assert(id1 != id2, "Job IDs are not unique")
    }
  }

  "creating a job" must "show up in dynamo" in withJob { id =>
    assert(dynamo.get(table, id).isDefined)
  }


}