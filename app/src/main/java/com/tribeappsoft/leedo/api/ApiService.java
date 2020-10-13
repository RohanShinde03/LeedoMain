package com.tribeappsoft.leedo.api;


import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public interface ApiService
{
    //-----------------------        Common API      ----------------------------------------//

    //download file
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    //POST_GenerateRazorPayOrder
    @POST(WebServer.POST_GenerateORDER)
    Call<JsonObject> generatePayOrder(
            @Header("Authorization") String authHeader,
            @Body JsonObject jsonObject);


    /* //getAppApiUser
     @GET(WebServer.Get_AppApiUser)
     Observable<Response<JsonObject>> getAppApiUser(
             @Query("id") int id);
 */
    //GET All Projects
    @GET(WebServer.GET_AllProjects)
    Call<JsonObject>getAllProjects(
            @Query("api_token") String api_token);

    @GET(WebServer.GET_AllProjects)
    Call<JsonObject>getUserWiseAllProjects(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);


    //GET All Projects
    @GET(WebServer.Get_AppApiUser)
    Call<JsonObject>getAppApiUser(@Query("id") int id);


    //Post_SalesLogin
    @POST(WebServer.Post_SalesLogin)
    Call<JsonObject> salesLogin(@Body JsonObject jsonObject);

    //user logout
    @POST(WebServer.POST_logoutFromApp)
    Call<JsonObject> logoutUser(@Body JsonObject jsonObject);

    //sendOTPToMobile
    @POST(WebServer.POST_SendOTPLeadMobile)
    Call<JsonObject> sendOTPLeadMobile(@Body JsonObject jsonObject);


    @POST(WebServer.POST_ForgotPasswordSendOTP)
    Call<JsonObject> ForgotPasswordSendOTP(@Body JsonObject jsonObject);

    //Post_studentForgotPasswordSendOTP
    @POST(WebServer.POST_UpdatePassword)
    Call<JsonObject> UpdatePassword(@Body JsonObject jsonObject);

    //updateStudentFCMToken
    @POST(WebServer.POST_UpdateFCM)
    Call<JsonObject> updateFCM(@Body JsonObject jsonObject);

    //GET check token validity status
    @GET(WebServer.GET_checkTokenValidity)
    Call<JsonObject>checkTokenValidity(
            @Query("api_token") String api_token,
            @Query("user_id") int user_id);



    //-----------------------        Admin Create Project      ----------------------------------------//

    //Post create project details
    @POST(WebServer.Post_createProject)
    Call<JsonObject> createProject(@Body JsonObject jsonObject);

    //Post update project details
    @POST(WebServer.Post_UpdateProjectDetails)
    Call<JsonObject> updateProjectDetails(@Body JsonObject jsonObject);

    //get all project list
    @GET(WebServer.GET_ProjectList)
    Observable<Response<JsonObject>> getProjectList(@Query("api_token") String api_token);

    //get project types
    @GET(WebServer.GET_ProjectTypes)
    Observable<Response<JsonObject>> getProjectTypes(@Query("api_token") String api_token);


    // -----------------------        New User API      ----------------------------------------//

    //Get All Prefixes
    @GET(WebServer.GET_AllPrefix)
    Observable<Response<JsonObject>> getAllPrefix
    (@Query("api_token") String api_token);

    //Post new User Details
    @POST(WebServer.Post_addNewUser)
    Call<JsonObject> addNewUser(@Body JsonObject jsonObject);

    //Post Update User Details
    @POST(WebServer.Post_UpdateUser)
    Call<JsonObject> UpdateUser(@Body JsonObject jsonObject);

    //get users role list
    @GET(WebServer.GET_UserRolesList)
    Observable<Response<JsonObject>> getUserRolesList(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);

    //get users list
    @GET(WebServer.GET_AllUsers)
    Observable<Response<JsonObject>> getAllUsers
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id);

    //get users list
    @GET(WebServer.GET_AllUsers)
    Call<JsonObject>getAllUser(@Query("api_token") String api_token, @Query("sales_person_id") int sales_person_id);

//-----------------------        Reminders API      ----------------------------------------//

    //Post_AddReminder
    @POST(WebServer.Post_Reminder) Call<JsonObject> addReminder(@Body JsonObject jsonObject);


    //Post_UpdateReminder
    @POST(WebServer.Post_UpdateReminder) Call<JsonObject> updateReminder(@Body JsonObject jsonObject);

    //Get All Reminders
    @GET(WebServer.Get_AllReminder) Observable<Response<JsonObject>> Get_AllReminder
    (@Query("api_token") String api_token,
     @Query("user_id") int user_id,
     @Query("page") int page,
     @Query("search_text") String search_text);



    //Post_Add Reminder
    @POST(WebServer.Post_MarkAsDone) Call<JsonObject> markAsDoneReminder(@Body JsonObject jsonObject);


    @POST(WebServer.Post_DeleteReminder) Call<JsonObject> deleteReminder(@Body JsonObject jsonObject);

    // -----------------------        Sales Home Feed API      ----------------------------------------//


    //get event details sales person
    @GET(WebServer.GET_EVENT_BANNERS_SALESPERSON)
    Call<JsonObject> getBookingEventBanners
    (@Query("api_token") String api_token);


    //Get Project List
    @GET(WebServer.GET_SalesFeed)
    Observable<Response<JsonObject>> getSalesFeed
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("limit") int limit,
     @Query("skip") int skip,
     @Query("filter_text") String filter_text,
     @Query("other_ids") String other_ids,
     @Query("last_lead_updated_at") String last_lead_updated_at);

    //Get Leads Report List
    @GET(WebServer.GET_SalesFeed)
    Observable<Response<JsonObject>> getSalesLeads
    (@Query("api_token") String api_token,
     @Query("project_id") int project_id,
     @Query("sales_person_id") int sales_person_id,
     @Query("lead_status_id") int lead_status_id,
     @Query("start_date") String start_date,
     @Query("end_date") String end_date,
     @Query("limit") int limit,
     @Query("skip") int skip,
     @Query("filter_text") String filter_text,
     @Query("other_ids") String other_ids,
     @Query("last_lead_updated_at") String last_lead_updated_at);





    //-----------------------        Leads API      ----------------------------------------//

    //Get CustomerID List
    @GET(WebServer.GET_CUIDData)
    Observable<Response<JsonObject>> getLeadList
    (@Query("api_token") String api_token,
     @Query("filter_text") String filter_text,
     @Query("for") int For,
     @Query("page") int page,
     @Query("sales_person_id") int sales_person_id,
     @Query("is_admin") int is_admin);


    @GET(WebServer.GET_UnClaimedLeads)
    Observable<Response<JsonObject>> getUnClaimedLeads
            (@Query("api_token") String api_token);

    @POST(WebServer.Post_LeadClaimNow)
    Call<JsonObject> addLeadClaimNow(@Body JsonObject jsonObject);


    //Get Project List
    @GET(WebServer.GET_LeadFormData)
    Observable<Response<JsonObject>> getLeadFormData
    (@Query("api_token") String api_token);

    @GET(WebServer.GET_LeadFormData)
    Observable<Response<JsonObject>> getLeadForm_Data
            (@Query("api_token") String api_token,
             @Query("sales_person_id") int sales_person_id);


    //get lead details
    @GET(WebServer.GET_LeadDetails) Call<JsonObject> getLeadDetails
            (@Query("api_token") String api_token,
             @Query("lead_id") int lead_id);


    //Post Add Sales Lead
    @POST(WebServer.POST_AddLead)
    Call<JsonObject> addSalesLead(@Body JsonObject jsonObject);

    //Post Update Lead
    @POST(WebServer.POST_UpdateLead)
    Call<JsonObject> updateLeadDetails(@Body JsonObject jsonObject);


    @GET(WebServer.GET_whatsAppNumber) Call<JsonObject>checkMobileNumberExistWhatsApp(
            @Query("api_token") String api_token,
            @Query("mobile_number") String mobile_number);


    @GET(WebServer.GET_whatsAppNumber) Call<JsonObject>checkMobileNumberExistOther(
            @Query("api_token") String api_token,
            @Query("alternate_mobile_number") String alternate_mobile_number);




    //-----------------------        Offline Leads API      ----------------------------------------//

    @POST(WebServer.POST_OfflineLeadsData)
    Call<JsonObject> add_OfflineLeads(@Body JsonObject jsonObject);


    //GET last synced time
    @GET(WebServer.Get_LastOfflineLeadSyncedTime)
    Call<JsonObject>getLastOfflineLeadSyncTime(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);


    //get all offline leads
    @GET(WebServer.Get_AllOfflineLeads) Observable<Response<JsonObject>> getAllDuplicateLeads(@Query("api_token") String api_token,
                                                                                              @Query("filter_text") String filter_text,
                                                                                              @Query("page") int page,
                                                                                              @Query("sales_person_id") int sales_person_id);
    //get duplicate lead details
    @GET(WebServer.Get_DuplicateLeadDetails) Call<JsonObject> getDuplicateLeadDetails(@Query("api_token") String api_token, @Query("offline_id") int offline_id);

    //post duplicate lead details
    @POST(WebServer.POST_updateDuplicateLeadDetails)
    Call<JsonObject> updateDuplicateLeadDetails(@Body JsonObject jsonObject);






    //-----------------------        Site Visits API      ----------------------------------------//

    //Get Site Visit Project List
    @GET(WebServer.Get_SiteVisitFromData)
    Observable<Response<JsonObject>> getSiteVisitProjectsList
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id);

    //Post_SiteVisit
    @POST(WebServer.Post_SiteVisit)
    Call<JsonObject> addSiteVisit(@Body JsonObject jsonObject);

    @GET(WebServer.GET_SiteVisitFilteredStat)
    Call<JsonObject> getSiteVisitStats
            (@Query("api_token") String api_token,
             @Query("start_date") String start_date,
             @Query("end_date") String end_date,
             @Query("sales_person_id") int sales_person_id,
             @Query("project_id") int project_id);



    //-----------------------        MArk As Book API      ----------------------------------------//


    //Get booking form data
    @GET(WebServer.Get_BookFormData)
    Observable<Response<JsonObject>> getBookFormData
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id);

    //book customer
    @Multipart
    @POST(WebServer.POST_AddBooking)
    Call<JsonObject> call_addBooking(
            @Part MultipartBody.Part profile,
            @Part("api_token") RequestBody api_token,
            @Part("lead_id") RequestBody lead_id,
            @Part("project_id") RequestBody project_id,
            @Part("unit_category_id") RequestBody unit_category_id,
            @Part("booking_datetime") RequestBody booking_datetime,
            @Part("unit_name") RequestBody unit_name,
            @Part("book_remark") RequestBody book_remark,
            @Part("media_type_id") RequestBody media_type_id,
            @Part("sales_person_id") RequestBody sales_person_id);
//-----------------------        Project Docs API      ----------------------------------------//

    //Get Project Docs
    @GET(WebServer.GET_ProjectDocs)
    Observable<Response<JsonObject>> getProjectDocs
    (@Query("api_token") String api_token,
     @Query("project_doc_type_id") int project_doc_type_id,
     @Query("sales_person_id") int sales_person_id);


    //add brochure
    @Multipart
    @POST(WebServer.POST_AddProjectDocs)
    Call<JsonObject> call_addProjectDocs(
            @Part("api_token") RequestBody api_token,
            @Part("project_id") RequestBody project_id,
            @Part("project_doc_type_id") RequestBody project_doc_type_id,
            @Part("doc_title") RequestBody brochure_title,
            @Part("doc_description") RequestBody brochure_description,
            @Part("media_type_id") RequestBody media_type_id,
            @Part MultipartBody.Part file_url);


    //update brochure
    @Multipart
    @POST(WebServer.POST_UpdateProjectDocs)
    Call<JsonObject> call_updateProjectDocs(
            @Part("api_token") RequestBody api_token,
            @Part("project_id") RequestBody project_id,
            @Part("project_doc_type_id") RequestBody project_doc_type_id,
            @Part("doc_title") RequestBody brochure_title,
            @Part("doc_description") RequestBody brochure_description,
            @Part("media_type_id") RequestBody media_type_id,
            @Part("project_doc_id") RequestBody project_doc_id,
            @Part MultipartBody.Part file_url);


    //delete project brochure
    @POST(WebServer.Post_DeleteProjectDocs)
    Call<JsonObject> deleteProjectDocs
    (@Body JsonObject jsonObject);




    //-----------------------        Tokens/GHP API      ----------------------------------------//

    //Get Token Details
    @GET(WebServer.Get_TokenData)
    Observable<Response<JsonObject>> GET_TokenData
    (@Query("api_token") String api_token);


    @Multipart
    @POST(WebServer.POST_KYCDocument)
    Call<JsonObject> add_KYCDocument(
            @Part MultipartBody.Part docFile,
            @Part("api_token") RequestBody api_token,
            @Part("doc_type_id") RequestBody doc_type_id,
            @Part("lead_id") RequestBody lead_id);

    //Post Generated Add Token + Site Visit
    @POST(WebServer.Post_generate_AddToken_siteVisit)
    Call<JsonObject> Post_generate_AddToken_siteVisit(@Body JsonObject jsonObject);

    //Post Generated Add Token
    @POST(WebServer.Post_generate_AddToken)
    Call<JsonObject> Post_generate_AddToken(@Body JsonObject jsonObject);

    //Post_updateLead
    @POST(WebServer.Post_updateLead)
    Call<JsonObject> Post_updateLead(@Body JsonObject jsonObject);

    //Post_updateLead
    @POST(WebServer.Post_updateLeadDetails)
    Call<JsonObject> Post_updateLeadDetails(@Body JsonObject jsonObject);

    //Post_updateLeadStage
    @POST(WebServer.PostUpdateLeadStage)
    Call<JsonObject> Post_updateLeadStage(@Body JsonObject jsonObject);

    //Post_updateLeadStage
    @POST(WebServer.PostChangeLeadStage)
    Call<JsonObject> Post_changeLeadStage(@Body JsonObject jsonObject);


    //-----------------------        Upgrade to GHP+ API      ----------------------------------------//

    //Get Token plus details
    @GET(WebServer.Get_TokenGHPInfo)
    Observable<Response<JsonObject>> Get_TokenGHPInfo
    (@Query("api_token") String api_token,
     @Query("event_id") int event_id,
     @Query("token_id") int token_id);


    //Post GHP Plus Details
    @POST(WebServer.Post_updateToken)
    Call<JsonObject> Post_updateToken(@Body JsonObject jsonObject);



    //-----------------------        Booking Events API      ----------------------------------------//

    //Post_EventListForApp
    @GET(WebServer.GET_BookingEventList)
    Observable<Response<JsonObject>> getBookingEvents
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("event_status_id") int event_status_id);


    //get event details sales person
    @GET(WebServer.GET_EVENT_DETAILS_SALESPERSON)
    Observable<Response<JsonObject>> getBookingEventDetails
    (@Query("api_token") String api_token, @Query("event_id") int event_id);




    //-----------------------        Hold Unit API      ----------------------------------------//


    //Get Project with Blocks
    @GET(WebServer.GET_allProjectWithBlock)
    Observable<Response<JsonObject>> getAllProjectWithBlocks
    (@Query("api_token") String api_token,
     @Query("event_id") int event_id);

    //Get All Inventory  with Blocks
    @GET(WebServer.GET_allInventoryByBlock)
    Observable<Response<JsonObject>> getAllInventoryWithBlocks
    (@Query("api_token") String api_token,
     @Query("block_id") int event_id);


    @GET(WebServer.GET_TokenInfo)
    Call<JsonObject> getTokenInfo
            (@Query("api_token") String api_token,
             @Query("event_id") int event_id,
             @Query("unit_id") int unit_id,
             @Query("sales_person_id") int sales_person_id,
             @Query("token_no") String token_no);



    //Get Hold flat inventory data
    @GET(WebServer.GET_FlatOnHoldBySalesPerson)
    Observable<Response<JsonObject>> getFlatOnHoldBySalesPerson
    (@Query("api_token") String api_token,
     @Query("event_id") int event_id,
     @Query("sales_person_id") int sales_person_id);


    //POST Hold Flat
    @POST(WebServer.POST_HoldFlat)
    Call<JsonObject> markAsHoldFlat(@Body JsonObject jsonObject);


    //Post Add Released Flat
    @POST(WebServer.POST_Flat_Released)
    Call<JsonObject> addReleasedFlat(@Body JsonObject jsonObject);




    //-----------------------        Direct Allotments API      ----------------------------------------//


    //DirectHoldFlat
    @POST(WebServer.POST_Direct_Release)
    Call<JsonObject> directReleaseFlat(@Body JsonObject jsonObject);

    //Get Hold flat direct data
    @GET(WebServer.GET_Direct_HoldFlatList)
    Observable<Response<JsonObject>> getDirectHoldFlats
    (@Query("api_token") String api_token,
     @Query("event_id") int event_id,
     @Query("sales_person_id") int sales_person_id,
     @Query("sales_head_data") int sales_head_data);


    //Get All Inventory of Available Flats with Blocks for direct booking
    @GET(WebServer.GET_allFloorsNAvailableFlat)
    Observable<Response<JsonObject>> getAllFloorsNAvailableFlat
    (@Query("api_token") String api_token,
     @Query("block_id") int event_id);

    //POST Hold Flat direct booking
    @POST(WebServer.POST_markUnitAsHold)
    Call<JsonObject> markUnitAsHold(@Body JsonObject jsonObject);


    //Post unit allotment
    @POST(WebServer.POST_UnitAllotment)
    Call<JsonObject> addUnitAllotment(@Body JsonObject jsonObject);

    @Multipart
    @POST(WebServer.POST_addBookingAttachments)
    Call<JsonObject> addBookingAttachments(
            @Part MultipartBody.Part docFile,
            @Part("booking_id") RequestBody booking_id,
            @Part("attachment_type_id") RequestBody attachment_type_id,
            @Part("api_token") RequestBody api_token);

    //Get Allotted flats
    @GET(WebServer.GET_getAllAllottedFlats)
    Observable<Response<JsonObject>> getAllAllottedFlats
    (@Query("sales_person_id") int sales_person_id,
     @Query("api_token") String api_token,
     @Query("is_admin") int is_admin);


    //POST cancel allotted flat
    @POST(WebServer.POST_CancelledBooking)
    Call<JsonObject> cancelAllotment(@Body JsonObject jsonObject);




    //-----------------------        Project Brochures API      ----------------------------------------//

    //Get Project Brochures
    @GET(WebServer.GET_ProjectBrochures)
    Observable<Response<JsonObject>> getProjectBrochures
    (@Query("api_token") String api_token);


    //add brochure
    @Multipart
    @POST(WebServer.POST_AddBrochure)
    Call<JsonObject> call_addProjectBrochure(
            @Part MultipartBody.Part profile,
            @Part("project_id") RequestBody project_id,
            @Part("brochure_title") RequestBody brochure_title,
            @Part("brochure_description") RequestBody brochure_description);


    //update brochure
    @Multipart
    @POST(WebServer.POST_AddBrochure)
    Call<JsonObject> call_updateProjectBrochure(
            @Part MultipartBody.Part profile,
            @Part("project_brochure_id") RequestBody project_brochure_id_,
            @Part("project_id") RequestBody project_id,
            @Part("brochure_title") RequestBody brochure_title,
            @Part("brochure_description") RequestBody brochure_description);


    //delete project brochure
    @POST(WebServer.Post_DeleteProjectBrochure)
    Call<JsonObject> deleteProjectBrochure
    (@Body JsonObject jsonObject);




    //-----------------------        Inventory API      ----------------------------------------//

    //Get inventory List
    @GET(WebServer.GET_InventoryData)
    Observable<Response<JsonObject>> GET_AllInventory
    (@Query("api_token") String api_token);




    //-----------------------        Performance API      ----------------------------------------//

    //GET Performance
    @GET(WebServer.GET_Performance)
    Observable<Response<JsonObject>> GetPerformance(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);

    //GET Performance
    @GET(WebServer.GET_Performance)
    Observable<Response<JsonObject>> GetPerformance(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date);



    //-----------------------        Call logs API      ----------------------------------------//

    //GET call status
    @GET(WebServer.GET_CallStatus)
    Call<JsonObject>getCallStatus(
            @Query("api_token") String api_token,
            @Query("user_id") int user_id);

    //Post Add Sales Lead
    @POST(WebServer.POST_AddCallLog)
    Call<JsonObject> addCallLog(@Body JsonObject jsonObject);



    //add call log multipart
    @Multipart
    @POST(WebServer.POST_AddCallLog)
    Call<JsonObject> addCallLog(

            @Part("call_log_status_id") RequestBody call_status_id,
            @Part("call_log_date") RequestBody call_log_date,
            @Part("start_time") RequestBody start_time,
            @Part("end_time") RequestBody end_time,
            @Part("lead_id") RequestBody lead_id,
            @Part("lead_status_id") RequestBody lead_status_id,
            @Part("call_remarks") RequestBody call_remarks,
            @Part("sales_person_id") RequestBody sales_person_id,
            @Part("call_schedule_id") RequestBody call_schedule_id,
            @Part("api_token") RequestBody api_token,
            @Part("remind_at") RequestBody remind_at,
            @Part("reminder_comments") RequestBody reminder_comments,
            @Part("is_reminder") RequestBody is_reminder,
            @Part("is_done") RequestBody is_done);

    //-----------------------        Update sales user API      ----------------------------------------//

    //upload sales Profile pic
    @Multipart
    @POST(WebServer.POST_updateSalesPersonsProfileImage)
    Call<JsonObject> updateSalesPersonsProfileImage(
            @Part MultipartBody.Part profile_path,
            @Part("api_token") RequestBody api_token,
            @Part("user_id") RequestBody staff_id);


    //Post Sales Profile
    @POST(WebServer.POST_UserDetails)
    Call<JsonObject> addUpdateUserDetails(@Body JsonObject jsonObject);




    //-----------------------        Team Lead API      ----------------------------------------//

    //Get inventory List
    @GET(WebServer.GET_TeamLeadPerformance)
    Observable<Response<JsonObject>> GetTeamLeadPerformance
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id);

    //GetTeamLeadPerformance List
    @GET(WebServer.GET_TeamLeadPerformance)
    Observable<Response<JsonObject>> GetTeamLeadPerformance
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("event_id") int event_id,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date);

    //GET_SalesManagerStats List
    @GET(WebServer.GET_SalesManagerStats)
    Observable<Response<JsonObject>> get_SalesManagerStats
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("event_id") int event_id,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date);


    //get team lead list
    @GET(WebServer.GET_TeamLeadList)
    Observable<Response<JsonObject>> getTeamLeadList(
            @Query("api_token") String api_token);


    //Post addSalesTeamLead
    @POST(WebServer.Post_addSalesTeamLead)
    Call<JsonObject> addSalesTeamLead(@Body JsonObject jsonObject);


    //Post_updateUpdateTeamLeadDetails
    @POST(WebServer.Post_UpdateTeamLeadDetails)
    Call<JsonObject> updateTeamLeadDetails(@Body JsonObject jsonObject);


    //get team member details sales person
    @GET(WebServer.GET_TEAM_LEAD_DETAILS_SALESPERSON)
    Observable<Response<JsonObject>> getSalesTeamLeadDetails
    (@Query("api_token") String api_token, @Query("user_id") int user_id);


    //update team members
    @POST(WebServer.POST_AddRemoveMember)
    Call<JsonObject> addRemoveTeamMembers(@Body JsonObject jsonObject);

    //team members list
    @GET(WebServer.GET_SalesExecutive)
    Observable<Response<JsonObject>> getSalesExecutives(
            @Query("api_token") String api_token);

    //Get inventory List
    @GET(WebServer.GET_TeamLeadWiseReport)
    Observable<Response<JsonObject>> GetTeamLeadWiseReport
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id);

    // GetTeamLeadWiseReport List
    @GET(WebServer.GET_TeamLeadWiseReport)
    Observable<Response<JsonObject>> GetTeamLeadWiseReport
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date);






    //-----------------------        Lead ReAssign API      ----------------------------------------//


    //get All leads by sales person
    @GET(WebServer.GET_AllLeadsBySalesPerson)
    Observable<Response<JsonObject>> getAllLeadsBySalesPerson(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("page") int page,
            @Query("filter_text") String search_text);



    //re-assign Leads to sales persons
    @POST(WebServer.POST_reAssignLeadsToSalesPersons)
    Call<JsonObject> reAssignLeadsToSalesPersons(@Body JsonObject jsonObject);






    //-----------------------        CP wise FOS API      ----------------------------------------//

    //Get cp Wise Report
    @GET(WebServer.GET_cpWiseReport)
    Observable<Response<JsonObject>>
    getCPWiseReport(
            @Query("api_token") String api_token,
            @Query("page") int page,
            @Query("filter_text") String search_text,
            @Query("project_id") long project_id);



    //Get Project List
    @GET(WebServer.GET_cpFOSWiseReport)
    Observable<Response<JsonObject>>
    getCPFOSWiseReport(
            @Query("cp_id") int cp_id);




    //-----------------------        sales Executives API      ----------------------------------------//


    //sales persons list
    @GET(WebServer.GET_getAllSalesPersons)
    Observable<Response<JsonObject>> getAllSalesPersons(
            @Query("api_token") String api_token);


    //Post_updateSalesExecutiveAsTeamLead
    @POST(WebServer.Post_updateSalesExecutive)
    Call<JsonObject> updateSalesExecutive(@Body JsonObject jsonObject);




    //-----------------------        Open Sale API      ----------------------------------------//

    //Get Project with Blocks
    @GET(WebServer.GET_AllProjectWiseBlocks)
    Observable<Response<JsonObject>> getAllProjectWithBlocks
    (@Query("api_token") String api_token);


    //update Blocks
    @POST(WebServer.POST_AddRemoveBlocks)
    Call<JsonObject> addRemoveBlocks(@Body JsonObject jsonObject);


    //Extend Hold time
    @POST(WebServer.POST_ExtendHoldTime)
    Call<JsonObject> extendHoldTime(@Body JsonObject jsonObject);



    //-----------------------     Churn Leads API      ----------------------------------------//


    //Get Project with Blocks
    @GET(WebServer.GET_UnclaimedLeads)
    Observable<Response<JsonObject>> getUnclaimedLeads12Hours
    (@Query("api_token") String api_token,
     @Query("limit") int limit,
     @Query("skip") int skip,
     @Query("filter_text") String search_text,
     @Query("project_id") long project_id,
     @Query("cp_executive_id") int cp_executive_id,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date,
     @Query("churn_count") int churn_count);


    //lead churned by auto
    @POST(WebServer.POST_unClaimedLeadAssignByAuto)
    Call<JsonObject> leadChurnedByAuto(@Body JsonObject jsonObject);


    //lead churned by manual
    @POST(WebServer.POST_unClaimedLeadAssignByManual)
    Call<JsonObject> leadChurnedByManual(@Body JsonObject jsonObject);


    //get ClaimedReassignLeads
    @GET(WebServer.GET_AllClaimedReassignLeads)
    Observable<Response<JsonObject>> getClaimedReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("cp_executive_id") int cp_executive_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);


    //get ClaimedReassignLeads
    @GET(WebServer.GET_AllClaimedReassignLeads)
    Observable<Response<JsonObject>> getClaimedReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("cp_executive_id") int cp_executive_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);



    //mark lead as unclaimed
    @POST(WebServer.POST_markLeadsAsUnclaimed)
    Call<JsonObject> markLeadsAsUnclaimed(@Body JsonObject jsonObject);





    //-----------------------------------   Sales Head Dashboard API  ---------------------------------------------//

    //GET All Projects
    @GET(WebServer.GET_FilteredStats)
    Observable<Response<JsonObject>>getFilteredStats(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date);

    //GET All Projects
    @GET(WebServer.GET_FilteredStats)
    Observable<Response<JsonObject>>getFilteredStats(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("event_id") int event_id,
            @Query("cp_id") int cp_id,
            @Query("lead_status_id") int lead_status_id);


    //GET lead summary count
    @GET(WebServer.GET_LeadSummaryReportCounts)
    Observable<Response<JsonObject>>getLeadSummaryReportCounts(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("event_id") int event_id,
            @Query("cp_id") int cp_id,
            @Query("lead_status_id") int lead_status_id,
            @Query("sales_team_lead_stats") boolean sales_team_lead_stats);


    //GET All Projects
    @GET(WebServer.GET_getAllSalesPersons)
    Call<JsonObject>getAllSalesPersonsList(
            @Query("api_token") String api_token);


    //GET CP List
    @GET(WebServer.GET_CPList)
    Call<JsonObject>getCPList(
            @Query("api_token") String api_token);

    //GET Lead Status List
    @GET(WebServer.GET_LeadStatusList)
    Call<JsonObject>getLeadStatusList(
            @Query("api_token") String api_token);




    //-----------------------------------   Team Lead Dashboard API  ---------------------------------------------//


    //GET All Filtered Team Leader Stat
    @GET(WebServer.GET_FilteredStatForTeamLeader)
    Observable<Response<JsonObject>>getFilteredStatsForTeamLeader(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("event_id") int event_id,
            @Query("cp_id") int cp_id,
            @Query("lead_status_id") int lead_status_id);






    //-----------------------------------   Team leader Stat and team stat details API  ---------------------------------------------//


    //GET_siteVisitDetails
    @GET(WebServer.Get_siteVisitDetails)
    Observable<Response<JsonObject>> getSiteVisitDetails(@Query("api_token") String api_token,
                                                         @Query("project_id") int project_id,
                                                         @Query("start_date") String start_date,
                                                         @Query("end_date") String end_date,
                                                         @Query("sales_person_id") int sales_person_id,
                                                         @Query("cp_executive_id") int cp_executive_id,
                                                         @Query("cp_id") int cp_id);

    //GET_leadDataDetails
    @GET(WebServer.Get_leadDataDetails)
    Observable<Response<JsonObject>> getLeadDataDetails(@Query("api_token") String api_token,
                                                        @Query("project_id") int project_id,
                                                        @Query("start_date") String start_date,
                                                        @Query("end_date") String end_date,
                                                        @Query("sales_person_id") int sales_person_id,
                                                        @Query("lead_status_id") int lead_status_id,
                                                        @Query("cp_executive_id") int cp_executive_id,
                                                        @Query("cp_id") int cp_id);

    //GET_LeadTokenDetails
    @GET(WebServer.Get_LeadTokenDetails)
    Observable<Response<JsonObject>> getLeadTokenDetails(@Query("api_token") String api_token,
                                                         @Query("project_id") int project_id,
                                                         @Query("start_date") String start_date,
                                                         @Query("end_date") String end_date,
                                                         @Query("sales_person_id") int sales_person_id,
                                                         @Query("lead_token_status_id") int lead_token_status_id,
                                                         @Query("cp_executive_id") int cp_executive_id,
                                                         @Query("cp_id") int cp_id);

    //GET_LeadTokenDetails
    @GET(WebServer.Get_bookingDetails)
    Observable<Response<JsonObject>> getStatBookingDetails(@Query("api_token") String api_token,
                                                           @Query("project_id") int project_id,
                                                           @Query("start_date") String start_date,
                                                           @Query("end_date") String end_date,
                                                           @Query("sales_person_id") int sales_person_id,
                                                           @Query("cp_executive_id") int cp_executive_id,
                                                           @Query("cp_id") int cp_id);

    //GET_BookingDetails
    @GET(WebServer.Get_bookingDetails)
    Observable<Response<JsonObject>> getStatBookingDetails(@Query("api_token") String api_token,
                                                           @Query("project_id") int project_id,
                                                           @Query("start_date") String start_date,
                                                           @Query("end_date") String end_date,
                                                           @Query("sales_person_id") int sales_person_id,
                                                           @Query("cp_executive_id") int cp_executive_id,
                                                           @Query("cp_id") int cp_id,
                                                           @Query("event_id") int event_id,
                                                           @Query("lead_status_id") int lead_status_id,
                                                           @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                           @Query("page") int page,
                                                           @Query("filter_text") String filter_text);


    //GET_CancelBookingDetails
    @GET(WebServer.Get_CancelBookingDetails)
    Observable<Response<JsonObject>> getStatCancelBookingDetails(@Query("api_token") String api_token,
                                                           @Query("project_id") int project_id,
                                                           @Query("start_date") String start_date,
                                                           @Query("end_date") String end_date,
                                                           @Query("sales_person_id") int sales_person_id,
                                                           @Query("cp_executive_id") int cp_executive_id,
                                                           @Query("cp_id") int cp_id,
                                                           @Query("event_id") int event_id,
                                                           @Query("lead_status_id") int lead_status_id,
                                                           @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                           @Query("page") int page,
                                                           @Query("filter_text") String filter_text);
    //GET_LeadTokenDetails
    @GET(WebServer.Get_LeadTokenDetails)
    Observable<Response<JsonObject>> getLeadTokenDetails(@Query("api_token") String api_token,
                                                         @Query("project_id") int project_id,
                                                         @Query("start_date") String start_date,
                                                         @Query("end_date") String end_date,
                                                         @Query("sales_person_id") int sales_person_id,
                                                         @Query("lead_token_status_id") int lead_token_status_id,
                                                         @Query("cp_executive_id") int cp_executive_id,
                                                         @Query("cp_id") int cp_id,
                                                         @Query("event_id") int event_id,
                                                         @Query("lead_status_id") int lead_status_id,
                                                         @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                         @Query("token_type_id") int token_type_id);

    //GET_LeadTokenDetails
    @GET(WebServer.Get_LeadTokenDetails)
    Observable<Response<JsonObject>> getLeadTokenDetails(@Query("api_token") String api_token,
                                                         @Query("project_id") int project_id,
                                                         @Query("start_date") String start_date,
                                                         @Query("end_date") String end_date,
                                                         @Query("sales_person_id") int sales_person_id,
                                                         @Query("lead_token_status_id") int lead_token_status_id,
                                                         @Query("cp_executive_id") int cp_executive_id,
                                                         @Query("cp_id") int cp_id,
                                                         @Query("event_id") int event_id,
                                                         @Query("lead_status_id") int lead_status_id,
                                                         @Query("sales_team_lead_stats") boolean sales_team_lead_stats);

    //GET_LeadTokenDetails
    @GET(WebServer.Get_LeadTokenDetails)
    Observable<Response<JsonObject>> getLeadTokenDetails_new(@Query("api_token") String api_token,
                                                             @Query("project_id") int project_id,
                                                             @Query("start_date") String start_date,
                                                             @Query("end_date") String end_date,
                                                             @Query("sales_person_id") int sales_person_id,
                                                             @Query("lead_token_status_id") int lead_token_status_id,
                                                             @Query("cp_executive_id") int cp_executive_id,
                                                             @Query("cp_id") int cp_id,
                                                             @Query("event_id") int event_id,
                                                             @Query("lead_status_id") int lead_status_id,
                                                             @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                             @Query("token_type_id") int token_type_id,
                                                             @Query("page") int page,
                                                             @Query("filter_text") String filter_text);

    //GET_siteVisitDetails
    @GET(WebServer.Get_siteVisitDetails)
    Observable<Response<JsonObject>> getSiteVisitDetails(@Query("api_token") String api_token,
                                                         @Query("project_id") int project_id,
                                                         @Query("start_date") String start_date,
                                                         @Query("end_date") String end_date,
                                                         @Query("sales_person_id") int sales_person_id,
                                                         @Query("lead_status_id") int lead_status_id,
                                                         @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                         @Query("page") int page,
                                                         @Query("filter_text") String filter_text);

    //GET_leadDataDetails
    @GET(WebServer.Get_leadDataDetails)
    Observable<Response<JsonObject>> getLeadDataDetails(@Query("api_token") String api_token,
                                                        @Query("project_id") int project_id,
                                                        @Query("start_date") String start_date,
                                                        @Query("end_date") String end_date,
                                                        @Query("sales_person_id") int sales_person_id,
                                                        @Query("lead_status_id") int lead_status_id,
                                                        @Query("cp_executive_id") int cp_executive_id,
                                                        @Query("cp_id") int cp_id,
                                                        @Query("event_id") int event_id,
                                                        @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                        @Query("page") int page,
                                                        @Query("filter_text") String filter_text);


    //GET_lead Details
    @GET(WebServer.Get_leadDataDetails)
    Observable<Response<JsonObject>> getLeadDetails(@Query("api_token") String api_token,
                                                        @Query("project_id") int project_id,
                                                        @Query("start_date") String start_date,
                                                        @Query("end_date") String end_date,
                                                        @Query("sales_person_id") int sales_person_id,
                                                        @Query("lead_status_id") int lead_status_id,
                                                        @Query("sales_team_lead_stats") boolean sales_team_lead_stats,
                                                        @Query("page") int page,
                                                        @Query("filter_text") String filter_text);


//


    //-----------------------------------   Site Visits Transfer API  ---------------------------------------------//


    //GET_SiteVisitedReassignLeads
    @GET(WebServer.GET_SiteVisitedReassignLeads)
    Observable<Response<JsonObject>> getSiteVisitedReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);


    //GET_SiteVisitedReassignLeads
    @GET(WebServer.GET_SiteVisitedReassignLeads)
    Observable<Response<JsonObject>> getSiteVisitedReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);


    //siteVisitLeadsTransfer
    @POST(WebServer.POST_siteVisitLeadsTransfer)
    Call<JsonObject> siteVisitLeadsTransfer(@Body JsonObject jsonObject);






    //-----------------------------------   GHP Transfer API  ---------------------------------------------//


    //GET_GhpReassignLeads
    @GET(WebServer.GET_GhpReassignLeads)
    Observable<Response<JsonObject>> getGhpReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);

    //GET_GhpReassignLeads
    @GET(WebServer.GET_GhpReassignLeads)
    Observable<Response<JsonObject>> getGhpReassignLeads(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("filter_text") String search_text);


    //GhpLeadsTransfer
    @POST(WebServer.POST_ghpLeadsTransfer)
    Call<JsonObject> ghpLeadsTransfer(@Body JsonObject jsonObject);





    //-----------------------------------  Home Screen API  ---------------------------------------------//

    //Get HomeLeads
    @GET(WebServer.GET_HomeLeads)
    Observable<Response<JsonObject>> getHomeLeads
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("project_id") int project_id,
     @Query("limit") int limit,
     @Query("skip") int skip,
     @Query("filter_text") String filter_text,
     @Query("other_ids") String other_ids,
     @Query("last_lead_updated_at") String last_lead_updated_at,
     @Query("date") String date,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date);

    //Get HomeSiteVisits
    @GET(WebServer.GET_HomeSiteVisits)
    Observable<Response<JsonObject>> getHomeSiteVisits
    (@Query("api_token") String api_token,
     @Query("sales_person_id") int sales_person_id,
     @Query("project_id") int project_id,
     @Query("limit") int limit,
     @Query("skip") int skip,
     @Query("filter_text") String filter_text,
     @Query("other_ids") String other_ids,
     @Query("last_lead_updated_at") String last_lead_updated_at,
     @Query("date") String date,
     @Query("from_date") String from_date,
     @Query("to_date") String to_date);



    //GET Call logs count Month Wise
    @GET(WebServer.GET_HomeCallScheduleListCount)
    Call<JsonObject>getHomeCallScheduleListCount(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);


    //GET HomeCallScheduleList
    @GET(WebServer.GET_HomeCallScheduleList)
    Observable<Response<JsonObject>> getHomeCallScheduleList(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("date") String date,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("page") int page,
            @Query("project_id") int project_id,
            @Query("filter_text") String filter_text);


    //Get Home Reminders
    @GET(WebServer.Get_HomeReminders) Observable<Response<JsonObject>> getHomeReminders
            (@Query("api_token") String api_token,
             @Query("sales_person_id") int user_id,
             @Query("project_id") int project_id,
             @Query("page") int page,
             @Query("filter_text") String search_text,
             @Query("date") String date,
             @Query("from_date") String from_date,
             @Query("to_date") String to_date);


    //GET Call logs count
    @GET(WebServer.GET_HomeAllCounts)
    Call<JsonObject>getHomeAllCounts(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("project_id") int project_id,
            @Query("date") String date,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date);

    //-----------------------------------   Call Schedule API  ---------------------------------------------//

    //GET Call logs count
    @GET(WebServer.GET_CallLogsCount)
    Call<JsonObject>getCallLogCounts(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("call_schedule_date") String call_schedule_date);


    //GET Call logs count Month Wise
    @GET(WebServer.GET_CallLogsCountMonthWise)
    Call<JsonObject>getCallLogCountsMonthWise(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id);


    //add call schedule
    @POST(WebServer.POST_ADD_CALL_SCHEDULE)
    Call<JsonObject> addCallSchedule(@Body JsonObject jsonObject);

    //add call re-schedule
    @POST(WebServer.POST_ADD_CALL_RESCHEDULE)
    Call<JsonObject> addCallRESchedule(@Body JsonObject jsonObject);

    //GET_CallScheduleList
    @GET(WebServer.GET_CallScheduleList)
    Observable<Response<JsonObject>> getScheduledCallLeads(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("call_schedule_date") String from_date,
            @Query("page") int page,
            @Query("project_id") int project_id,
            @Query("filter_text") String filter_text);


    //GET_CallScheduleCompletedList
    @GET(WebServer.GET_CallScheduleCompletedList)
    Observable<Response<JsonObject>> getCompletedCallLeads(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("call_schedule_date") String from_date,
            @Query("page") int page,
            @Query("project_id") int project_id,
            @Query("filter_text") String filter_text);




    //-----------------------------------   Sales Head call Log stats API  ---------------------------------------------//


    //GET All CallLoges
    @GET(WebServer.GET_ScheduledCallStats)
    Observable<Response<JsonObject>>getAllCallLogesStats(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("sales_team_lead_stats") boolean sales_team_lead_stats);


    //call Filter schedule list
    @GET(WebServer.GET_CallScheduleList)
    Observable<Response<JsonObject>> getFilterScheduledCallLeads(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("call_schedule_date") String call_schedule_date,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("page") int page,
            @Query("project_id") int project_id,
            @Query("schedule_status_id") int schedule_status_id,
            @Query("filter_text") String filter_text,
            @Query("sales_team_lead_stats") boolean sales_team_lead_stats);


    //GET_Filter CompletedList
    @GET(WebServer.GET_CallScheduleCompletedList)
    Observable<Response<JsonObject>> getFilterCompletedCallLeads(
            @Query("api_token") String api_token,
            @Query("sales_person_id") int sales_person_id,
            @Query("call_schedule_date") String call_schedule_date,
            @Query("from_date") String from_date,
            @Query("to_date") String to_date,
            @Query("page") int page,
            @Query("project_id") int project_id,
            @Query("filter_text") String filter_text,
            @Query("sales_team_lead_stats") boolean sales_team_lead_stats);



    //-----------------------------------   Export to Excel API  ---------------------------------------------//


    //GET Export to Excel
    @GET(WebServer.Get_ExportToExcel)
    Call<ResponseBody> getExportToExcel(
            @Query("api_token") String api_token,
            @Query("project_id") int project_id,
            @Query("sales_person_id") int sales_person_id,
            @Query("start_date") String start_date,
            @Query("end_date") String end_date,
            @Query("exportFormat") String exportFormat
    );

    //POST_Upload file to Drive
    @Multipart
    @POST(WebServer.POST_UploadTODrive)
    Call<JsonObject> uploadFileToDrive(
            @Part MultipartBody.Part fileMedia,
            @Part("api_token") RequestBody api_token,
            @Part("uploadType") RequestBody uploadType,
            @Part("upload_id") RequestBody upload_id);




    //-----------------------------------   unused api's ---------------------------------------------//


    //GET User Profile Info
    @GET(WebServer.GET_SalesUserInfo)
    Call<JsonObject>getUserProfileInfo(
            @Query("api_token") String api_token,
            @Query("user_id") int user_id);



    //GET RegisteredEventList
    @GET(WebServer.GET_RegisteredEventList)
    Observable<Response<JsonObject>> getStudentRegisteredEventList(@Query("api_token") String api_token,
                                                                   @Query("participant_id") int participant_id,
                                                                   @Query("participant_type") int participant_type);

    //updateStudentFCMToken
    @POST(WebServer.POST_UpdateStudentFCM)
    Call<JsonObject> updateStudentFCM(@Body JsonObject jsonObject);


    //GET_Colleague List
    @GET(WebServer.Get_Colleague_List)
    Observable<Response<JsonObject>> getStaffColleague
    (@Query("api_token") String api_token, @Query("staff_id") int staff_id);


    //upload student Profile pic
    @Multipart
    @POST(WebServer.POST_updateStaffProfileImage)
    Call<JsonObject> updateStaffProfileImage(
            @Part MultipartBody.Part profile_path,
            @Part("api_token") RequestBody api_token,
            @Part("staff_id") RequestBody staff_id);

    //GET All Projects
    @GET(WebServer.GET_TEAM_LEAD_DETAILS_SALESPERSON)
    Call<JsonObject>getSalesPersonsByTeamLead(
            @Query("api_token") String api_token,
            @Query("user_id") int user_id);



}
