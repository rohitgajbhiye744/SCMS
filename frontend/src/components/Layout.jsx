import { Outlet, Navigate, NavLink, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, LayoutDashboard, PlusCircle, List, Settings, BarChart2, Menu, X } from 'lucide-react';

export default function Layout() {
    const { user, logout, loading } = useAuth();
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const location = useLocation();

    // Close mobile menu when route changes
    useEffect(() => {
        setIsMobileMenuOpen(false);
    }, [location]);

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return (
        <div className="h-screen bg-[#FFFBF5] flex flex-col md:flex-row overflow-hidden">
            {/* Mobile Header */}
            <div className="md:hidden flex items-center justify-between bg-[#F7EFE5] border-b border-[#C3ACD0]/40 p-4 flex-shrink-0 z-20">
                <div className="flex items-center space-x-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-[#674188]"></span>
                    <h1 className="text-xl font-bold text-[#674188] tracking-tight">SCMS</h1>
                </div>
                <button
                    onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    className="p-2 -mr-2 text-[#5a446a] hover:text-[#674188] focus:outline-none"
                >
                    {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                </button>
            </div>

            {/* Overlay for mobile */}
            {isMobileMenuOpen && (
                <div 
                    className="fixed inset-0 bg-[#281B34]/40 z-10 md:hidden" 
                    onClick={() => setIsMobileMenuOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside className={`${
                isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
            } fixed inset-y-0 left-0 z-20 w-64 bg-[#F7EFE5] border-r border-[#C3ACD0]/40 transition-transform duration-200 ease-in-out md:static md:translate-x-0 flex flex-col h-full flex-shrink-0`}>
                <div className="h-16 flex items-center px-6 border-b border-[#C3ACD0]/40 hidden md:flex flex-shrink-0 space-x-2.5">
                    <span className="w-3 h-3 rounded-full bg-[#674188]"></span>
                    <h1 className="text-xl font-bold text-[#674188] tracking-tight">SCMS</h1>
                </div>
                <div className="p-4 flex flex-col flex-1 overflow-y-auto">
                    <nav className="space-y-1.5 flex-1">
                        <NavLink to="/dashboard" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${isActive ? 'bg-[#674188] text-white shadow-sm' : 'text-[#5a446a] hover:text-[#674188] hover:bg-[#C3ACD0]/25'}`}>
                            <LayoutDashboard className="mr-3 h-5 w-5" />
                            Dashboard
                        </NavLink>
                        {user.role === 'ROLE_REPORTER' && (
                            <NavLink to="/complaints/new" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${isActive ? 'bg-[#674188] text-white shadow-sm' : 'text-[#5a446a] hover:text-[#674188] hover:bg-[#C3ACD0]/25'}`}>
                                <PlusCircle className="mr-3 h-5 w-5" />
                                New Complaint
                            </NavLink>
                        )}
                        {user.role !== 'ROLE_REPORTER' && (
                            <NavLink to="/complaints" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${isActive ? 'bg-[#674188] text-white shadow-sm' : 'text-[#5a446a] hover:text-[#674188] hover:bg-[#C3ACD0]/25'}`}>
                                <List className="mr-3 h-5 w-5" />
                                {user.role === 'ROLE_STAFF' ? 'Assigned Tasks' : 'All Complaints'}
                            </NavLink>
                        )}
                        {user.role === 'ROLE_ADMIN' && (
                            <NavLink to="/analytics" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${isActive ? 'bg-[#674188] text-white shadow-sm' : 'text-[#5a446a] hover:text-[#674188] hover:bg-[#C3ACD0]/25'}`}>
                                <BarChart2 className="mr-3 h-5 w-5" />
                                Analytics
                            </NavLink>
                        )}

                    </nav>
                    
                    <div className="border-t border-[#C3ACD0]/40 pt-4 flex-shrink-0">
                        <div className="px-3 py-2 bg-[#FFFBF5]/70 rounded-lg border border-[#C3ACD0]/30 mb-2">
                            <p className="text-sm font-semibold text-[#281B34] truncate">{user.username}</p>
                            <p className="text-xs text-[#7A668A] truncate">{user.email}</p>
                        </div>
                        <button
                            onClick={logout}
                            className="w-full flex items-center px-3 py-2 text-sm font-medium rounded-lg text-[#5a446a] hover:text-[#674188] hover:bg-[#C3ACD0]/25 transition-colors"
                        >
                            <LogOut className="mr-3 h-5 w-5" />
                            Sign out
                        </button>
                    </div>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 h-full overflow-y-auto bg-[#FFFBF5] p-4 sm:p-6 md:p-8 w-full">
                <Outlet />
            </main>
        </div>
    );
}
