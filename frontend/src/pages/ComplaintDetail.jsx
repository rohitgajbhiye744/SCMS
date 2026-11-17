import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, Clock, MapPin, User, AlertTriangle } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export default function ComplaintDetail() {
    const { id } = useParams();
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [statusUpdate, setStatusUpdate] = useState('');
    const [remarks, setRemarks] = useState('');
    const [selectedStaff, setSelectedStaff] = useState('');

    const { data: complaint, isLoading: loading } = useQuery({
        queryKey: ['complaint', id],
        queryFn: async () => {
            const response = await api.get(`/complaints/${id}`);
            return response.data;
        }
    });

    const { data: staffList = [] } = useQuery({
        queryKey: ['staff', user.departmentId],
        queryFn: async () => {
            const response = await api.get(`/users/staff?departmentId=${user.departmentId}`);
            return response.data;
        },
        enabled: user.role === 'ROLE_DEPARTMENT_COORDINATOR' && !!user.departmentId
    });

    useEffect(() => {
        if (complaint && !statusUpdate) {
            setStatusUpdate(complaint.status);
        }
    }, [complaint, statusUpdate]);

    const statusMutation = useMutation({
        mutationFn: async ({ status, remarks }) => {
            await api.patch(`/complaints/${id}/status`, null, {
                params: { status, remarks }
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['complaint', id] });
            queryClient.invalidateQueries({ queryKey: ['complaints'] });
            setRemarks('');
        }
    });

    const assignMutation = useMutation({
        mutationFn: async (staffId) => {
            await api.patch(`/complaints/${id}/assign?staffId=${staffId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['complaint', id] });
            queryClient.invalidateQueries({ queryKey: ['complaints'] });
            setSelectedStaff('');
        }
    });

    const handleStatusUpdate = async (e) => {
        e.preventDefault();
        statusMutation.mutate({ status: statusUpdate, remarks });
    };

    const handleAssign = async (e) => {
        e.preventDefault();
        assignMutation.mutate(selectedStaff);
    };

    if (loading) return <div className="p-8">Loading details...</div>;
    if (!complaint) return <div className="p-8">Complaint not found.</div>;

    const isUpdating = statusMutation.isPending;
    const isAssigning = assignMutation.isPending;
    const canUpdateStatus = user.role === 'ROLE_STAFF' || user.role === 'ROLE_DEPARTMENT_COORDINATOR';

    return (
        <div className="space-y-6 max-w-5xl mx-auto">
            <div className="flex items-center space-x-4">
                <Link to="/complaints" className="p-2 bg-white rounded-lg shadow-sm hover:bg-[#F7EFE5] border border-[#C3ACD0]/50 text-[#674188] transition-colors">
                    <ArrowLeft className="h-5 w-5" />
                </Link>
                <h1 className="text-2xl font-bold text-[#281B34] tracking-tight">Complaint #{complaint.id}</h1>
                <span className={`px-2.5 py-0.5 text-xs font-medium rounded-full border ${
                    complaint.status === 'Resolved' || complaint.status === 'Closed' ? 'bg-[#F7EFE5] text-[#674188] border-[#C3ACD0]' :
                    complaint.status === 'In Progress' ? 'bg-[#674188] text-white border-[#674188]' :
                    'bg-[#C3ACD0]/20 text-[#543372] border-[#C3ACD0]'
                }`}>
                    {complaint.status}
                </span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Main Details */}
                <div className="lg:col-span-2 space-y-6">
                    <div className="bg-white rounded-xl shadow-sm border border-[#C3ACD0]/40 p-6">
                        <h2 className="text-xl font-semibold text-[#281B34] mb-4">{complaint.title}</h2>
                        <div className="prose max-w-none text-[#543372] mb-6 leading-relaxed">
                            {complaint.description}
                        </div>
                        
                        {complaint.imageUrls && complaint.imageUrls.length > 0 && (
                            <div>
                                <h3 className="text-sm font-semibold text-[#281B34] mb-3">Attached Images</h3>
                                <div className="flex flex-wrap gap-4">
                                    {complaint.imageUrls.map((url, idx) => (
                                        <a key={idx} href={url} target="_blank" rel="noopener noreferrer">
                                            <img src={url} alt="Complaint Attachment" className="w-32 h-32 object-cover rounded-lg border border-[#C3ACD0]/50" />
                                        </a>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Management Section */}
                    {(canUpdateStatus || (user.role === 'ROLE_DEPARTMENT_COORDINATOR' && !complaint.assignedStaffEmail)) && (
                        <div className="bg-white rounded-xl shadow-sm border border-[#C3ACD0]/40 p-6">
                            <h3 className="text-lg font-semibold text-[#281B34] mb-6">Manage Complaint</h3>
                            <div className="space-y-8">
                                {/* Assignment Form for Coordinator */}
                                {user.role === 'ROLE_DEPARTMENT_COORDINATOR' && (!complaint.assignedStaffEmail) && (
                                    <form onSubmit={handleAssign} className="space-y-4 pb-8 border-b border-[#C3ACD0]/30">
                                        <div>
                                            <label className="block text-sm font-medium text-[#281B34] mb-1">Assign to Staff</label>
                                            <div className="flex space-x-3">
                                                <select
                                                    value={selectedStaff}
                                                    onChange={(e) => setSelectedStaff(e.target.value)}
                                                    className="flex-1 px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] bg-white text-sm text-[#281B34]"
                                                    required
                                                >
                                                    <option value="">-- Select Staff Member --</option>
                                                    {staffList.map(staff => (
                                                        <option key={staff.id} value={staff.id}>{staff.username} ({staff.email})</option>
                                                    ))}
                                                </select>
                                                <button
                                                    type="submit"
                                                    disabled={isAssigning || !selectedStaff}
                                                    className="px-4 py-2 bg-[#674188] text-white text-sm font-medium rounded-lg hover:bg-[#543372] disabled:opacity-50 transition-colors whitespace-nowrap shadow-sm"
                                                >
                                                    {isAssigning ? 'Assigning...' : 'Assign'}
                                                </button>
                                            </div>
                                        </div>
                                    </form>
                                )}

                                {/* Status Update Form for Staff/Admin */}
                                {canUpdateStatus && (
                                    <form onSubmit={handleStatusUpdate} className="space-y-4">
                                        <div>
                                            <label className="block text-sm font-medium text-[#281B34] mb-1">Update Status</label>
                                            <select
                                                value={statusUpdate}
                                                onChange={(e) => setStatusUpdate(e.target.value)}
                                                className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] bg-white text-sm text-[#281B34]"
                                            >
                                                <option value="In Progress">In Progress</option>
                                                <option value="Resolved">Resolved</option>
                                                <option value="Closed">Closed</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-[#281B34] mb-1">Remarks</label>
                                            <textarea
                                                value={remarks}
                                                onChange={(e) => setRemarks(e.target.value)}
                                                rows={3}
                                                placeholder="Add remarks about the update..."
                                                className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] text-sm text-[#281B34]"
                                                required
                                            />
                                        </div>
                                        <button
                                            type="submit"
                                            disabled={isUpdating}
                                            className="px-4 py-2 bg-[#674188] text-white text-sm font-medium rounded-lg hover:bg-[#543372] disabled:opacity-50 transition-colors shadow-sm"
                                        >
                                            {isUpdating ? 'Updating...' : 'Update Status'}
                                        </button>
                                    </form>
                                )}
                            </div>
                        </div>
                    )}
                </div>

                {/* Sidebar Details */}
                <div className="space-y-6">
                    <div className="bg-white rounded-xl shadow-sm border border-[#C3ACD0]/40 p-6">
                        <h3 className="text-xs font-semibold text-[#674188] uppercase tracking-wider mb-4">Complaint Info</h3>
                        <div className="space-y-4">
                            <div className="flex items-start space-x-3">
                                <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                    <Clock className="h-4 w-4 text-[#674188]" />
                                </div>
                                <div>
                                    <p className="text-xs font-medium text-[#7A668A]">Submitted On</p>
                                    <p className="text-sm font-medium text-[#281B34]">{new Date(complaint.dateSubmitted).toLocaleString()}</p>
                                </div>
                            </div>
                            <div className="flex items-start space-x-3">
                                <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                    <User className="h-4 w-4 text-[#674188]" />
                                </div>
                                <div>
                                    <p className="text-xs font-medium text-[#7A668A]">Reporter</p>
                                    <p className="text-sm font-medium text-[#281B34]">{complaint.userEmail}</p>
                                </div>
                            </div>
                            <div className="flex items-start space-x-3">
                                <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                    <AlertTriangle className="h-4 w-4 text-[#674188]" />
                                </div>
                                <div>
                                    <p className="text-xs font-medium text-[#7A668A]">Category & Priority</p>
                                    <p className="text-sm font-medium text-[#281B34]">{complaint.category} • {complaint.priority}</p>
                                </div>
                            </div>
                            {complaint.roomNumber && (
                                <div className="flex items-start space-x-3">
                                    <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                        <MapPin className="h-4 w-4 text-[#674188]" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-[#7A668A]">Room Number</p>
                                        <p className="text-sm font-medium text-[#281B34]">{complaint.roomNumber}</p>
                                    </div>
                                </div>
                            )}
                            {complaint.departmentName && (
                                <div className="flex items-start space-x-3">
                                    <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                        <MapPin className="h-4 w-4 text-[#674188]" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-[#7A668A]">Department</p>
                                        <p className="text-sm font-medium text-[#281B34]">{complaint.departmentName}</p>
                                    </div>
                                </div>
                            )}
                            {complaint.assignedStaffEmail && (
                                <div className="flex items-start space-x-3">
                                    <div className="p-1.5 bg-[#C3ACD0]/20 rounded-lg">
                                        <User className="h-4 w-4 text-[#674188]" />
                                    </div>
                                    <div>
                                        <p className="text-xs font-medium text-[#7A668A]">Assigned Staff</p>
                                        <p className="text-sm font-medium text-[#281B34]">{complaint.assignedStaffEmail}</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
