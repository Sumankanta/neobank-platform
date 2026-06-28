import { environment } from '../../../environments/environment';
import { API_ROUTES } from '../constants/api-routes';

export class ApiEndpoints {
  static auth = {
    login: () => `${environment.apiUrl}${API_ROUTES.AUTH}/login`,
    register: () => `${environment.apiUrl}${API_ROUTES.AUTH}/register`,
    verifyDocument: (idNumber: string) => `${environment.apiUrl}${API_ROUTES.AUTH}/verify-document?idNumber=${idNumber}`,
    requestOtp: () => `${environment.apiUrl}${API_ROUTES.AUTH}/otp/request`,
    verifyOtp: () => `${environment.apiUrl}${API_ROUTES.AUTH}/otp/verify`,
  };

  static users = {
    profile: () => `${environment.apiUrl}${API_ROUTES.USERS}/me`,
  };

  static admin = {
    // These go through /api/users/admin/... (UserController)
    users: () => `${environment.apiUrl}${API_ROUTES.USERS}/admin/users`,
    toggleStatus: (id: number | string) => `${environment.apiUrl}${API_ROUTES.USERS}/admin/users/${id}/toggle-status`,
    // These go through /api/admin/... (new AdminController)
    pendingApprovals: () => `${environment.apiUrl}${API_ROUTES.ADMIN}/pending-approvals`,
    dashboard: () => `${environment.apiUrl}${API_ROUTES.ADMIN}/dashboard`,
    systemHealth: () => `${environment.apiUrl}${API_ROUTES.ADMIN}/system-health`,
    // All accounts (admin view through /api/accounts)
    allAccounts: () => `${environment.apiUrl}${API_ROUTES.ACCOUNTS}`,
    // All transactions (admin view through /api/admin/transactions)
    allTransactions: () => `${environment.apiUrl}${API_ROUTES.ADMIN}/transactions`,
  };


  static loans = {
    products: () => `${environment.apiUrl}${API_ROUTES.LOANS}/products`,
    apply: () => `${environment.apiUrl}${API_ROUTES.LOANS}/apply`,
    myApplications: () => `${environment.apiUrl}${API_ROUTES.LOANS}/my-applications`,
    adminApplications: () => `${environment.apiUrl}${API_ROUTES.LOANS}/admin/applications`,
    decision: (id: number | string) => `${environment.apiUrl}${API_ROUTES.LOANS}/${id}/decision`,
    myAccounts: () => `${environment.apiUrl}${API_ROUTES.LOANS}/my-accounts`,
    repayments: (id: number | string) => `${environment.apiUrl}${API_ROUTES.LOANS}/${id}/repayments`,
  };

  static insights = {
    summary: (userId: number | string) => `${environment.apiUrl}${API_ROUTES.INSIGHTS}/${userId}`,
  };

  static accounts = {
    list: () => `${environment.apiUrl}${API_ROUTES.ACCOUNTS}`,
    create: () => `${environment.apiUrl}${API_ROUTES.ACCOUNTS}`,
    detail: (id: number | string) => `${environment.apiUrl}${API_ROUTES.ACCOUNTS}/${id}`,
    transactions: (accountId: number | string) => `${environment.apiUrl}${API_ROUTES.ACCOUNTS}/${accountId}/transactions`,
  };

  static budgets = {
    list: () => `${environment.apiUrl}${API_ROUTES.BUDGETS}`,
    create: () => `${environment.apiUrl}${API_ROUTES.BUDGETS}`,
    summary: (userId: number | string, month: string) => `${environment.apiUrl}${API_ROUTES.BUDGETS}/${userId}/${month}`,
    delete: (id: number | string) => `${environment.apiUrl}${API_ROUTES.BUDGETS}/${id}`,
  };

  static bills = {
    list: () => `${environment.apiUrl}${API_ROUTES.BILLS}`,
    create: () => `${environment.apiUrl}${API_ROUTES.BILLS}`,
    detail: (id: number | string) => `${environment.apiUrl}${API_ROUTES.BILLS}/${id}`,
    status: (id: number | string) => `${environment.apiUrl}${API_ROUTES.BILLS}/${id}/status`,
  };

  static rewards = {
    balance: (userId: number | string) => `${environment.apiUrl}${API_ROUTES.REWARDS}/${userId}`,
  };

  static transactions = {
    history: () => `${environment.apiUrl}${API_ROUTES.TRANSACTIONS}/history`,
    summary: () => `${environment.apiUrl}${API_ROUTES.TRANSACTIONS}/summary`,
  };

  static statements = {
    download: () => `${environment.apiUrl}/statements/download`,
  };
}
