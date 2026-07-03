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
        <div className="min-h-screen bg-gray-50 flex flex-col md:flex-row">
            {/* Mobile Header */}
            <div className="md:hidden flex items-center justify-between bg-white border-b border-gray-200 p-4 sticky top-0 z-20">
                <h1 className="text-xl font-bold text-gray-900 tracking-tight">SCMS</h1>
                <button
                    onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    className="p-2 -mr-2 text-gray-500 hover:text-gray-700 focus:outline-none"
                >
                    {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                </button>
            </div>

            {/* Overlay for mobile */}
            {isMobileMenuOpen && (
                <div 
                    className="fixed inset-0 bg-gray-600 bg-opacity-50 z-10 md:hidden" 
                    onClick={() => setIsMobileMenuOpen(false)}
                />
            )}

            {/* Sidebar */}
            <aside className={`${
                isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
            } fixed inset-y-0 left-0 z-20 w-64 bg-white border-r border-gray-200 transition-transform duration-200 ease-in-out md:relative md:translate-x-0 flex flex-col`}>
                <div className="h-16 flex items-center px-6 border-b border-gray-200 hidden md:flex">
                    <h1 className="text-xl font-bold text-gray-900 tracking-tight">SCMS</h1>
                </div>
                <div className="p-4 flex flex-col flex-1 overflow-y-auto">
                    <nav className="space-y-1 flex-1">
                        <NavLink to="/dashboard" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${isActive ? 'bg-gray-100 text-gray-900' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'}`}>
                            <LayoutDashboard className="mr-3 h-5 w-5" />
                            Dashboard
                        </NavLink>
                        {user.role === 'ROLE_REPORTER' && (
                            <NavLink to="/complaints/new" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${isActive ? 'bg-gray-100 text-gray-900' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'}`}>
                                <PlusCircle className="mr-3 h-5 w-5" />
                                New Complaint
                            </NavLink>
                        )}
                        {user.role !== 'ROLE_REPORTER' && (
                            <NavLink to="/complaints" className={({isActive}) => `flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${isActive ? 'bg-gray-100 text-gray-900' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'}`}>
                                <List className="mr-3 h-5 w-5" />
                                {user.role === 'ROLE_STAFF' ? 'Assigned Tasks' : 'All Complaints'}
                            </NavLink>
                        )}

                    </nav>
                    
                    <div className="border-t pt-4">
                        <div className="px-2 py-2">
                            <p className="text-sm font-medium text-gray-700 truncate">{user.username}</p>
                            <p className="text-xs text-gray-500 truncate">{user.email}</p>
                        </div>
                        <button
                            onClick={logout}
                            className="mt-2 w-full flex items-center px-3 py-2 text-sm font-medium rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-colors"
                        >
                            <LogOut className="mr-3 h-5 w-5" />
                            Sign out
                        </button>
                    </div>
                </div>
            </aside>

            {/* Main content */}
            <main className="flex-1 overflow-y-auto bg-gray-50 p-4 sm:p-6 md:p-8 w-full">
                <Outlet />
            </main>
        </div>
    );
}
