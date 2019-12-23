## Money Transfer  API
REST API for transferring money from one account to another.


### Technologies
* Framework: Spark Java.
* Dependency Injection: Google Guice.
* Testing: Junit.
* Mocking: Mockito.
* Data Store: In memory Map.

### How to Run the Application
* mvn exec:java
* java -jar transfer-api-1.0-SNAPSHOT-jar-with-dependencies.jar (from target folder after building application)

### Testing
* Unit Testing
* Integration Testing

### Supported Features 
* Create account.
* Get account.
* Deposit money.
* Withdraw money.
* Transfer money from one account to another account.
* Delete all accounts.
 
          

### API Endpoint Details

 Path | Method | description
--- | --- | ---
 /accounts | POST | Create a new account
 /accounts/:accountId |GET | Get account information
 /accounts/withdraw | PUT| Withdraw money into account
 /accounts/deposit | PUT | Deposit money into account
 /accounts/transfer | POST | Transfer money from one account to another account
  /accounts | DELETE | Delete all accounts

#### Sample JSON

##### Create Account : POST
    Request:
        {
        "userId": "test user",
        "balance": "1000"    
        }
    
    Response:
        Status : 201
        "6a3c9e30-a9ff-40aa-b05f-52abf0baacb1"
        
##### Get Account Information : GET
    Request:
        localhost:4567/accounts/6a3c9e30-a9ff-40aa-b05f-52abf0baacb1
    
    Response:
        {
        "accountId":"6a3c9e30-a9ff-40aa-b05f-52abf0baacb1",
        "userId":"test user",
        "balance":1000
        }             
        
##### Withdraw Money : PUT
    Request:
        {
         "accountId" : "6a3c9e30-a9ff-40aa-b05f-52abf0baacb1",
         "amount" : "120"
        }       
    Response:
        Status : 200
        "Amount has been withdrawal"        
                
##### Deposit money : PUT
    Request:
        {
         "accountId" : "d1f40220-3aea-44b8-91f0-930faef6df3c",
         "amount" : "200"
        }        
    Response:
        Status : 200
        "Amount has been deposited"   
        
  
        
##### Transfer money : POST        
    Request:
        {
             "fromAccount" : "6a3c9e30-a9ff-40aa-b05f-52abf0baacb1",
             "toAccount" : "86960df4-3fb4-4962-8c91-03ea5276d0e8",
             "amount" : "500"
        }
    Response:
        Status : 200    
        "Money has been transferred successfully"
        
        
##### Delete All Accounts : DELETE
    Request:
        localhost:4567/accounts/eb989689-dd6d-4c2c-8bc8-9a9dd3dc1541
     
    Response:
        Status : 200
        "All accounts have been deleted"

        
### Https Status
* 200 OK: The request has succeeded
* 201 Created: Request has been succeeded has led to the creation of a resource
* 400 Bad Request: The request could not be understood by the server
* 404 Not Found: The requested resource cannot be found
* 409 Conflict: The request conflict with current state of the server
* 500 Internal Server Error: The server encountered an unexpected condition 

