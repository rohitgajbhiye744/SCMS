import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import api from '../api/axios';
import { Camera, Upload, X } from 'lucide-react';

export default function NewComplaint() {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const [images, setImages] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [departments, setDepartments] = useState([]);

    useEffect(() => {
        api.get('/departments')
            .then(res => setDepartments(res.data))
            .catch(console.error);
    }, []);

    const handleImageChange = (e) => {
        if (e.target.files) {
            const filesArray = Array.from(e.target.files);
            setImages(prev => [...prev, ...filesArray]);
        }
    };

    const removeImage = (index) => {
        setImages(prev => prev.filter((_, i) => i !== index));
    };

    const onSubmit = async (data) => {
        try {
            setIsSubmitting(true);
            setError('');
            
            const formData = new FormData();
            formData.append('title', data.title);
            formData.append('description', data.description);
            formData.append('category', data.category);
            formData.append('priority', data.priority);
            
            if (data.departmentId) {
                formData.append('departmentId', data.departmentId);
            }
            if (data.roomNumber) {
                formData.append('roomNumber', data.roomNumber);
            }
            if (data.qrCodeUuid) {
                formData.append('qrCodeUuid', data.qrCodeUuid);
            }
            
            images.forEach(image => {
                formData.append('images', image);
            });

            await api.post('/complaints', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to submit complaint');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto bg-white p-6 sm:p-8 rounded-xl shadow-sm border border-[#C3ACD0]/40">
            <h1 className="text-2xl font-bold text-[#281B34] tracking-tight mb-6">File a New Complaint</h1>
            
            {error && (
                <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-6 border border-red-200 text-sm">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Title</label>
                    <input
                        type="text"
                        {...register('title', { required: 'Title is required' })}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] text-[#281B34] placeholder-[#7A668A]"
                        placeholder="e.g., Broken projector in Room 101"
                    />
                    {errors.title && <span className="text-xs text-red-500 mt-1">{errors.title.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Room Number</label>
                    <input
                        type="text"
                        {...register('roomNumber', { required: 'Room Number is required' })}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] text-[#281B34] placeholder-[#7A668A]"
                        placeholder="e.g., 101, Lab 2, Main Hall"
                    />
                    {errors.roomNumber && <span className="text-xs text-red-500 mt-1">{errors.roomNumber.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Department</label>
                    <select
                        {...register('departmentId', { required: 'Department is required' })}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] bg-white text-[#281B34]"
                    >
                        <option value="">Select a department</option>
                        {departments.map(dept => (
                            <option key={dept.id} value={dept.id}>{dept.name}</option>
                        ))}
                    </select>
                    {errors.departmentId && <span className="text-xs text-red-500 mt-1">{errors.departmentId.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Category</label>
                    <select
                        {...register('category', { required: 'Category is required' })}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] bg-white text-[#281B34]"
                    >
                        <option value="">Select a category</option>
                        <option value="Electrical">Electrical</option>
                        <option value="Plumbing">Plumbing</option>
                        <option value="Network/WiFi">Network/WiFi</option>
                        <option value="Cleanliness">Cleanliness</option>
                        <option value="Furniture">Furniture</option>
                        <option value="IT Equipment">IT Equipment</option>
                        <option value="Security">Security</option>
                        <option value="Other">Other</option>
                    </select>
                    {errors.category && <span className="text-xs text-red-500 mt-1">{errors.category.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Priority</label>
                    <select
                        {...register('priority', { required: 'Priority is required' })}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] bg-white text-[#281B34]"
                    >
                        <option value="Low">Low</option>
                        <option value="Medium">Medium</option>
                        <option value="High">High</option>
                        <option value="Critical">Critical</option>
                    </select>
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-1">Description</label>
                    <textarea
                        {...register('description', { required: 'Description is required' })}
                        rows={4}
                        className="w-full px-4 py-2 border border-[#C3ACD0]/60 rounded-lg focus:ring-2 focus:ring-[#674188] focus:border-[#674188] text-[#281B34] placeholder-[#7A668A]"
                        placeholder="Describe the issue in detail..."
                    />
                    {errors.description && <span className="text-xs text-red-500 mt-1">{errors.description.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-[#281B34] mb-2">Images</label>
                    <div className="flex flex-wrap gap-4 mb-4">
                        {images.map((img, index) => (
                            <div key={index} className="relative w-24 h-24 rounded-lg border border-[#C3ACD0]/60 overflow-hidden">
                                <img src={URL.createObjectURL(img)} alt={`Preview ${index}`} className="object-cover w-full h-full" />
                                <button
                                    type="button"
                                    onClick={() => removeImage(index)}
                                    className="absolute top-1 right-1 bg-[#674188] text-white rounded-full p-1 hover:bg-[#543372]"
                                >
                                    <X size={14} />
                                </button>
                            </div>
                        ))}
                    </div>
                    <div className="flex space-x-4">
                        <label className="flex items-center justify-center px-4 py-2 border border-[#C3ACD0]/70 rounded-lg shadow-sm text-sm font-medium text-[#543372] bg-[#F7EFE5]/50 hover:bg-[#F7EFE5] cursor-pointer transition-colors">
                            <Upload className="mr-2 h-5 w-5 text-[#674188]" />
                            Upload Images
                            <input
                                type="file"
                                multiple
                                accept="image/*"
                                className="hidden"
                                onChange={handleImageChange}
                            />
                        </label>
                        <label className="flex items-center justify-center px-4 py-2 border border-[#C3ACD0]/70 rounded-lg shadow-sm text-sm font-medium text-[#543372] bg-[#F7EFE5]/50 hover:bg-[#F7EFE5] cursor-pointer transition-colors">
                            <Camera className="mr-2 h-5 w-5 text-[#674188]" />
                            Take Photo
                            <input
                                type="file"
                                accept="image/*"
                                capture="environment"
                                className="hidden"
                                onChange={handleImageChange}
                            />
                        </label>
                    </div>
                </div>

                <div className="pt-4 border-t border-[#C3ACD0]/30">
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full flex justify-center py-3 px-4 rounded-lg shadow-sm text-sm font-medium text-white bg-[#674188] hover:bg-[#543372] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#674188] disabled:opacity-50 transition-colors"
                    >
                        {isSubmitting ? 'Submitting...' : 'Submit Complaint'}
                    </button>
                </div>
            </form>
        </div>
    );
}
