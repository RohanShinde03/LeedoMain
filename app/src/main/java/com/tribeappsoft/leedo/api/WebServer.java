package com.tribeappsoft.leedo.api;

/**
 * Created by Rohan on 07/05/19.
 * */

public class WebServer {



    //TODO BASE URL LIVE
    public static final String BASE_URL = "http://15.207.242.165/1tr/suk09/public/api/";

    //TODO BASE URL LEEDO DEMO ENV
    //public static final String BASE_URL = "http://15.207.242.165/leedo/lead_management_sp1/public/api/";

    private static final String BASE_URL_LOGO = "http://vjpartners.co.in/ongoing/v7_new/vj-sales-modules/public/";


    public static final String API_TOKEN_EXTERNAL = "WeweSJdhbbgfuysfgbkjnfakjsndfkajsdnlaksdadZASCXADA";

    //paymentLogo
    public static final String paymentLogo = BASE_URL_LOGO + "designImg/vj_logo.jpg";

    public static final String VJ_Website = "www.leedo.com";



    //-----------------------        Common API      ----------------------------------------//

    //POST_getOrderID
    public static final String POST_GenerateORDER = "https://api.razorpay.com/v1/orders";

    //getAppApiUser
    public static final String Get_AppApiUser = BASE_URL + "getAppApiUser";

    //GET Project Names
    public static final String GET_AllProjects = BASE_URL + "getAllProjectNames";

    //POST Student-Login
    public static final String Post_SalesLogin = BASE_URL + "salesLogin";

    //logout from app
    public static final String POST_logoutFromApp = BASE_URL + "userLogout";

    //post send otp lead mobile
    public static final String POST_SendOTPLeadMobile = BASE_URL + "sendOtp";

    //POST ForgotPasswordSendOTP
    public static final String POST_ForgotPasswordSendOTP = BASE_URL + "forgotPasswordSendOTPToMobile";

    //POST UpdatePassword
    public static final String POST_UpdatePassword = BASE_URL + "changePassword";

    //updateFCM
    public static final String POST_UpdateFCM = BASE_URL + "addAndUpdateFcmToken";

    //GET check token validity status
    public static final String GET_checkTokenValidity = BASE_URL + "checkTokenValidity";

    //POST Change Password
    public static final String POST_ChangePassword = BASE_URL + "forgotPassword";



    //-----------------------       Admin Create Project      ----------------------------------------//

    //Post create project details
    public static final String Post_createProject = BASE_URL + "addProject";

    //Post update project details
    public static final String Post_UpdateProjectDetails = BASE_URL + "updateProject";

    //get project list
    public static final String GET_ProjectList =BASE_URL + "getAllProjects";

    //get project types
    public static final String GET_ProjectTypes =BASE_URL + "getProjectTypes";


    // -----------------------        New User API      ----------------------------------------//

    //Get AllPrefix
    public static final String GET_AllPrefix = BASE_URL + "getAllPrefix";

    //Post add NewUser Details
    public static final String Post_addNewUser = BASE_URL + "addSalesUser";

    //Post update User Details
    public static final String Post_UpdateUser = BASE_URL + "updateSalesUser";

    //get users role list
    public static final String GET_UserRolesList =BASE_URL + "getAllRoles";

    //get users list
    public static final String GET_AllUsers =BASE_URL + "getAllUsers";

    //-----------------------        Reminders API      ----------------------------------------//

    //Post Reminder
    public static final String Post_Reminder = BASE_URL + "addReminder";

    //Post Update Reminder
    public static final String Post_UpdateReminder = BASE_URL + "updateReminder";

    //Get Reminder
    public static final String Get_AllReminder = BASE_URL + "getAllReminders";

    //Post Mark as done Reminder
    public static final String Post_MarkAsDone = BASE_URL + "markAsDone";

    //Post Delete Reminder
    public static final String Post_DeleteReminder = BASE_URL + "deleteReminder";





    //-----------------------        Sales Home Feed API      ----------------------------------------//

    //GET_Event banners feed
    public static final String GET_EVENT_BANNERS_SALESPERSON = BASE_URL + "getBookingEventBanner";

    //Get salesFeed
    public static final String GET_SalesFeed = BASE_URL + "getSalesFeed";








    //-----------------------        Leads API      ----------------------------------------//

    //Get CUID
    public static final String GET_CUIDData = BASE_URL + "getLeadList";

    //Get Unclaimed Leads
    public static final String GET_UnClaimedLeads = BASE_URL + "getUnClaimedLeads";

    //POST claim lead
    public static final String Post_LeadClaimNow = BASE_URL + "claimLead";

    //Get LeadFormData
    public static final String GET_LeadFormData = BASE_URL + "getLeadFormData";

    //POST Add Lead
    public static final String POST_AddLead = BASE_URL + "addSalesLead";

    //POST Update Lead
    public static final String POST_UpdateLead = BASE_URL + "updateLeadDetails";

    //check duplicate number for add enquiry
    public static final String GET_whatsAppNumber = BASE_URL + "checkMobileNumberExist";

    //Get LeadDetails
    public static final String GET_LeadDetails = BASE_URL + "getLeadDetails";



    //-----------------------        Offline Leads API      ----------------------------------------//


    public static final String POST_OfflineLeadsData = BASE_URL + "offlineleads/add";
    //getLastSyncedTime
    public static final String Get_LastOfflineLeadSyncedTime = BASE_URL + "offlineleads/lastsync";
    //get all offline leads
    public static final String Get_AllOfflineLeads = BASE_URL + "offlineleads";
    //get duplicate lead details
    public static final String Get_DuplicateLeadDetails = BASE_URL + "offlineleads";
    //post duplicate lead details
    public static final String POST_updateDuplicateLeadDetails = BASE_URL + "offlineleads/update";




    //-----------------------        Site Visit API      ----------------------------------------//

    //Get Site Visit/
    public static final String Get_SiteVisitFromData = BASE_URL + "getSiteVisitData";

    //Post Site Visit
    public static final String Post_SiteVisit = BASE_URL + "addSiteVisit";

    //Site Visit Stat
    public static final String GET_SiteVisitFilteredStat = BASE_URL + "getSiteVisitStats";


    //-----------------------        MArk As Book API      ----------------------------------------//

    //Get BookFormData
    public static final String Get_BookFormData = BASE_URL + "getBookFormData";

    //book unit
    public static final String POST_AddBooking = BASE_URL + "addBooking";

//-----------------------        Project Docs API      ----------------------------------------//

    //Get Project docs
    public static final String GET_ProjectDocs = BASE_URL + "getAllProjectDocs";

    //add project docs
    public static final String POST_AddProjectDocs = BASE_URL + "addProjectDoc";

    //update project docs
    public static final String POST_UpdateProjectDocs = BASE_URL + "updateProjectDoc";

    //delete project docs
    public static final String Post_DeleteProjectDocs = BASE_URL + "deleteProjectDocs";

    // project doc liked and change sort
    public static final String Post_ProjectDocLiked = BASE_URL + "markAsDocLiked";

    // project doc unLiked and change sort
    public static final String Post_ProjectDocUnLiked = BASE_URL + "markAsDocUnLiked";


    //-----------------------        Tokens API      ----------------------------------------//

    //Get Token Details
    public static final String Get_TokenData = BASE_URL + "getTokenData";

    //add lead kyc docs
    public static final String POST_KYCDocument = BASE_URL + "addUploadKyc";

    //Post Generate Add Token Site Visit Details
    public static final String Post_generate_AddToken_siteVisit = BASE_URL + "addTokenWithSiteVisit";

    //Post Generate Add Token Details
    public static final String Post_generate_AddToken = BASE_URL + "addToken";

    //Post Update Lead
    public static final String Post_updateLead = BASE_URL + "updateLeadContactDetails";

    public static final String Post_updateLeadDetails = BASE_URL + "updateLeadPersonDetails";

    public static final String PostUpdateLeadStage = BASE_URL + "updateLeadStage";

    public static final String PostChangeLeadStage = BASE_URL + "changeLeadStage";



    //-----------------------        Upgrade to GHP+ API      ----------------------------------------//

    //Get Token Details
    public static final String Get_TokenGHPInfo = BASE_URL + "getTokenGHPInfo";

    //Post updateToken GHP Plus Details
    public static final String Post_updateToken = BASE_URL + "updateToken";




    //-----------------------        Booking Events API      ----------------------------------------//

    //GET_Sales BookingEventList
    public static final String GET_BookingEventList = BASE_URL + "getBookingEvents";

    //GET_Event details
    public static final String GET_EVENT_DETAILS_SALESPERSON = BASE_URL + "getBookingEventDetails";




    //-----------------------        Hold Unit API      ----------------------------------------//

    //GET All project with blocks
    public static final String GET_allProjectWithBlock = BASE_URL + "getAllProjectsWithBlocks";

    //GET All Inventory By block
    public static final String GET_allInventoryByBlock = BASE_URL + "getAllInventoryByBlock";

    //GET
    public static final String GET_TokenInfo= BASE_URL + "getTokenInfo";

    //Get LeadFormData
    public static final String GET_FlatOnHoldBySalesPerson = BASE_URL + "getFlatOnHoldBySalesPerson";

    //POST hold flat
    public static final String POST_HoldFlat = BASE_URL + "markAsHold";

    //POST Released flat
    public static final String POST_Flat_Released = BASE_URL + "markAsRelease";





    //-----------------------        Direct Allotments API      ----------------------------------------//


    //direct release flat
    public static final String POST_Direct_Release = BASE_URL + "markUnitAsRelease";
    //get direct hold flats list
    public static final String GET_Direct_HoldFlatList = BASE_URL + "getUnitOnHoldBySalesPerson";

    //GET All Inventory of available flats By block
    public static final String GET_allFloorsNAvailableFlat = BASE_URL + "getAllFloorsNAvailableFlat";

    //POST hold flat
    public static final String POST_markUnitAsHold = BASE_URL + "markUnitAsHold";

    //POST add UnitAllotment
    public static final String POST_UnitAllotment = BASE_URL + "UnitAllotment";

    //add allotment attachments
    public static final String POST_addBookingAttachments = BASE_URL + "addBookingAttachments";

    //Get Project wise All AllottedFlats
    public static final String GET_getAllAllottedFlats = BASE_URL + "getAllAllottedFlats";

    //POST hold flat
    //public static final String POST_CancelledFlat = BASE_URL + "cancelAllotment";

    //POST hold flat
    public static final String POST_CancelledBooking = BASE_URL + "cancelBooking";





    //-----------------------        Project Brochures API      ----------------------------------------//

    //Get Project Brochure
    public static final String GET_ProjectBrochures = BASE_URL + "getAllProjectBrochures";

    //add brochure
    public static final String POST_AddBrochure = BASE_URL + "addProjectBrochure";

    //delete brochure
    public static final String Post_DeleteProjectBrochure = BASE_URL + "deleteProjectBrochure";



    //-----------------------        Inventory API      ----------------------------------------//

    //Get all inventory Data
    public static final String GET_InventoryData = BASE_URL + "getAllInventory";



    //-----------------------        Performance API      ----------------------------------------//

    //GET Performance
    public static final String GET_Performance = BASE_URL + "getPerformance";




    //-----------------------        Call logs API      ----------------------------------------//

    //GET Sales Person Profile info
    public static final String GET_CallStatus = BASE_URL + "getAllCallLogStatus";

    //POST Add Lead
    public static final String POST_AddCallLog = BASE_URL + "addLeadCallLog";







    //-----------------------        Update sales user API      ----------------------------------------//

    //POST update SalesPersons Profile pic
    public static final String POST_updateSalesPersonsProfileImage =  BASE_URL + "updateUserPhoto";

    //POST addUpdateUserDetails
    public static final String POST_UserDetails = BASE_URL + "updateUserDetails";





    //-----------------------        Team Lead API      ----------------------------------------//

    //GET TeamLeadPerformance
    public static final String GET_TeamLeadPerformance = BASE_URL + "getTeamLeadPerformance";

    //getSalesManagerStats
    public static final String GET_SalesManagerStats = BASE_URL + "getSalesManagerStats";

    //get sales lead list
    public static final String GET_TeamLeadList =BASE_URL + "getSalesTeamLeadList";

    //Post addSalesTeamLead
    public static final String Post_addSalesTeamLead = BASE_URL + "addSalesTeamLead";

    //Post Update TeamLeadDetails
    public static final String Post_UpdateTeamLeadDetails = BASE_URL + "updateSalesTeamLead";

    //GET_Team Lead details
    public static final String GET_TEAM_LEAD_DETAILS_SALESPERSON = BASE_URL + "salesTeamLeadDetails";

    //update team member
    public static final String POST_AddRemoveMember =BASE_URL+"addUpdateSalesTeamMember";

    //team members list
    public static final String GET_SalesExecutive =BASE_URL+"salesExcutiveList";

    //GET Team Lead Wise report
    public static final String GET_TeamLeadWiseReport = BASE_URL + "getAllTeamLeadPerformance";



    //-----------------------        Lead ReAssign API      ----------------------------------------//


    //get All leads
    public static final String GET_AllLeadsBySalesPerson = BASE_URL + "getAllLeadsBySalesPerson";


    //re-assign Leads to sales persons
    public static final String POST_reAssignLeadsToSalesPersons =BASE_URL+"leadreassign";






    //-----------------------        CP wise FOS API      ----------------------------------------//

    //get cp wise Report
    public static final String GET_cpWiseReport = BASE_URL + "getReportCpWiseStats";



    //get cp FOS wise Report
    public static final String GET_cpFOSWiseReport = BASE_URL + "getCpWiseFosReportStats";



    //-----------------------        sales Executives API      ----------------------------------------//

    //sales persons list
    public static final String GET_getAllSalesPersons =BASE_URL+"getAllSalesPersons";


    //Post updateSalesExecutiveAsTeamLead
    public static final String Post_updateSalesExecutive = BASE_URL + "updateSalesExecutive";




    //-----------------------        Open Sale API      ----------------------------------------//

    //GET All project with blocks
    public static final String GET_AllProjectWiseBlocks = BASE_URL + "getAllProjectsWithBlocks";

    //POST update blocks
    public static final String POST_AddRemoveBlocks = BASE_URL + "setUnsetBlocksOfs";

    //Extend Hold by N Min
    public static final String POST_ExtendHoldTime  = BASE_URL + "extendHoldTime";





    //-----------------------     Churn Leads API      ----------------------------------------//

    //GET Un-claimed leads
    public static final String GET_UnclaimedLeads = BASE_URL + "getUnClaimedLeadsLast12Hours";

    //Lead Churned byAuto
    public static final String POST_unClaimedLeadAssignByAuto =BASE_URL + "unClaimedLeadAssignByAuto";

    //Lead Churned byManual
    public static final String POST_unClaimedLeadAssignByManual =BASE_URL + "unClaimedLeadAssignByManual";

    //get unclaimed reassign list
    public static final String GET_AllClaimedReassignLeads = BASE_URL + "claimedReassignLeads";

    //markLeadsAsUnclaimed
    public static final String POST_markLeadsAsUnclaimed =BASE_URL+"markLeadsAsUnClaimed";








    //-----------------------------------   Sales Head Dashboard API  ---------------------------------------------//

    //Get FilteredStats
    public static final String GET_FilteredStats = BASE_URL + "getFilteredStats";

    //get lead summary count
    public static final String GET_LeadSummaryReportCounts = BASE_URL + "getLeadSummaryReportCounts";

    public static final String GET_CPList = BASE_URL + "getCpNames";

    public static final String GET_LeadStatusList = BASE_URL + "getLeadStatuses";




    //-----------------------------------   Team Lead Dashboard API  ---------------------------------------------//

    //Get FilteredStatsForTeamLeader
    public static final String GET_FilteredStatForTeamLeader = BASE_URL + "getFilteredStatsForTL";





    //-----------------------------------   Team leader Stat and team stat details API  ---------------------------------------------//

    //GET_siteVisitDetails
    public static final String Get_siteVisitDetails = BASE_URL + "getSiteVisitsDetails";

    //GET_LeadDataDetails
    public static final String Get_leadDataDetails = BASE_URL + "getLeadDataDetails";

    //GET_LeadTokenDetails
    public static final String Get_LeadTokenDetails = BASE_URL + "getLeadTokensDetails";

    //GET_BookingDetails
    public static final String Get_bookingDetails = BASE_URL + "getBookingDataDetails";

    //GET_CancelBookingDetails
    public static final String Get_CancelBookingDetails = BASE_URL + "getCancelBookingDataDetails";



    //-----------------------------------   Site Visits Transfer API  ---------------------------------------------//

    //get site visit reassign list
    public static final String GET_SiteVisitedReassignLeads = BASE_URL + "siteVisitedReassignLeads";

    //siteVisitLeadsTransfer
    public static final String POST_siteVisitLeadsTransfer =BASE_URL+"siteVisitLeadsTransfer";



    //-----------------------------------   GHP Transfer API  ---------------------------------------------//

    //get GET_GhpReassignLeads reassign list
    public static final String GET_GhpReassignLeads = BASE_URL + "ghpReassignLeads";


    //siteVisitLeadsTransfer
    public static final String POST_ghpLeadsTransfer =BASE_URL+"ghpLeadsTransfer";


    //-----------------------------------  Home Screen API  ---------------------------------------------//

    //Get Home leads
    public static final String GET_HomeLeads = BASE_URL + "getHomeLeads";

    //Get Home site visits
    public static final String GET_HomeSiteVisits = BASE_URL + "getHomeSiteVisits";

    //GET HomeCallScheduleListCount
    public static final String GET_HomeCallScheduleListCount = BASE_URL + "getHomeCallScheduleListCount";

    //get HomeCallScheduleList
    public static final String GET_HomeCallScheduleList = BASE_URL + "getHomeCallScheduleList";

    //Get Home Reminder
    public static final String Get_HomeReminders = BASE_URL + "getHomeReminders";

    //get all home counts
    public static final String GET_HomeAllCounts = BASE_URL + "getHomeAllCounts";



    //-----------------------------------   Call Schedule API  ---------------------------------------------//

    //get GET_GhpReassignLeads reassign list
    public static final String GET_CallLogsCount = BASE_URL + "getSchedulesCompleteCountDateWise";



    //GET Count month wise
    public static final String GET_CallLogsCountMonthWise = BASE_URL + "getSchedulesCompleteCountMonthWise";

    //POST Add call schedule
    public static final String POST_ADD_CALL_SCHEDULE = BASE_URL + "addCallSchedule";

    //POST add call re-schedule
    public static final String POST_ADD_CALL_RESCHEDULE = BASE_URL + "addCallReSchedule";

    //get CallScheduleList
    public static final String GET_CallScheduleList = BASE_URL + "getCallScheduleList";

    //get CallScheduleCompletedList
    public static final String GET_CallScheduleCompletedList = BASE_URL + "getCallScheduleCompletedList";




    //-----------------------------------   Sales Head call Log stats API  ---------------------------------------------//

    //Get FilteredStats
    public static final String GET_ScheduledCallStats = BASE_URL + "getCallLogReportCounts";





    //-----------------------------------   Export to Excel API  ---------------------------------------------//

    //GET_ExportToExcel
    public static final String Get_ExportToExcel = BASE_URL + "getLeadDataExports";

    //POST Upload file to drive
    public static final String POST_UploadTODrive = "https://www.googleapis.com/upload/drive/v3/files";


    //-----------------------------------   unused api's ---------------------------------------------//


    //GET Sales Person Profile info
    public static final String GET_SalesUserInfo = BASE_URL + "staff/getSalesUserInfo";



    //GET_StudentEventList
    public static final String GET_StudentEventList = BASE_URL + "student/getEventListForApp";


    //GET_Event details list for student
    public static final String GET_EVENT_DETAILS_STUDENTS = BASE_URL + "student/getEventDetailsForApp";


    //GET RegisteredEventList
    public static final String GET_RegisteredEventList = BASE_URL + "student/getAllRegisteredEventList";


    //POST studentForgotPasswordSendOTP
    public static final String POST_studentForgotPasswordSendOTP = BASE_URL + "studentForgetPasswordSendOtp";

    //POST studentUpdatePassword
    public static final String POST_studentUpdatePassword = BASE_URL + "updateStudentPassword";

    //updateStudentFCM
    public static final String POST_UpdateStudentFCM = BASE_URL + "student/addAndUpdateFcmToken";

    //GET_ColleagueListForApp
    public static final String Get_Colleague_List = BASE_URL + "staff/getColleagues";


    //POST update Staff Profile pic
    public static final String POST_updateStaffProfileImage =  BASE_URL + "staff/updateStaffProfileImage";

}
